
package org.eduai.educhat.service.discussion.impl


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eduai.educhat.dto.message.MessageDto
import org.eduai.educhat.dto.discussion.request.EnterThreadRequestDto
import org.eduai.educhat.dto.discussion.request.RestoreThreadRequestDto
import org.eduai.educhat.dto.message.request.SendMessageRequestDto
import org.eduai.educhat.dto.discussion.response.EnterThreadResponseDto
import org.eduai.educhat.dto.discussion.response.RestoreThreadResponseDto
import org.eduai.educhat.entity.DiscThreadHist
import org.eduai.educhat.repository.ClsMstRepository
import org.eduai.educhat.repository.DiscGrpMemRepository
import org.eduai.educhat.repository.DiscThreadHistRepository
import org.eduai.educhat.service.discussion.ChannelManageService
import org.eduai.educhat.service.KeyGeneratorService
import org.eduai.educhat.service.discussion.ThreadManageService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Service
class ThreadManageServiceImpl(
    private val redisTemplate: StringRedisTemplate,
    private val channelManageService: ChannelManageService,
    private val keyGenService: KeyGeneratorService,
    private val clsRepo: ClsMstRepository,
    private val grpMemRepo: DiscGrpMemRepository,
    private val discThreadHistRepository: DiscThreadHistRepository
) : ThreadManageService {


    companion object {
        private val logger = LoggerFactory.getLogger(ThreadManageServiceImpl::class.java)
        private const val LOCK_KEY = "message_flush_lock"
        private const val LOCK_TTL = 10
    }

    override fun createGroupChannel(clsId: String, groupId: UUID) {
        val sessionListKey = keyGenService.generateRedisSessionKey(clsId)
        val sessionGrpKey = keyGenService.generateRedisSessionHashKey(groupId.toString())
        val topicName = "chat:$groupId"

        channelManageService.createGroupChannel(topicName)
        redisTemplate.opsForHash<String, String>().put(sessionListKey, sessionGrpKey, topicName)

        logger.info("채팅방 생성 완료: $topicName (Group ID: $groupId)")
    }

    override fun removeGroupChannel(clsId: String, groupId: UUID) {
        val grpId = groupId.toString()
        val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(grpId)
        val topicName = "chat:$groupId"

        channelManageService.removeGroupChannel(topicName)
        redisTemplate.opsForHash<String, String>().delete(clsSessionKey, grpSessionKey)

        flushMessagesToDB(clsId, grpId)

        logger.info("채팅방 삭제 완료: (Group ID: $groupId)")
    }

    override fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto) {
        val clsId = sendMessageRequestDto.clsId
        val grpId = sendMessageRequestDto.grpId

        val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(grpId)

        val topicName = redisTemplate.opsForHash<String, String>().get(clsSessionKey, grpSessionKey)
            ?: throw IllegalArgumentException("유효한 채널이 아님")

        val messageDto = MessageDto(
            clsId = clsId,
            sender = sendMessageRequestDto.sender,
            grpId = grpId,
            message = sendMessageRequestDto.message,
            timestamp = Instant.now().toString()
        )

        val messageJson = jacksonObjectMapper().writeValueAsString(messageDto)

        saveMessageLog(clsId, grpId, messageDto)

        redisTemplate.convertAndSend(topicName, messageJson)

        logger.info("📤 Redis 전송됨: $messageJson → 채널: $topicName")
    }

    override fun saveMessageLog(clsId: String, grpId: String, messageDto: MessageDto) {
        val redisKey = keyGenService.generatePendingMessagesKey(clsId, grpId)

        redisTemplate.opsForList().rightPush(redisKey, jacksonObjectMapper().writeValueAsString(messageDto))

        val messageCount = redisTemplate.opsForList().size(redisKey) ?: 0
        if (messageCount >= 10) {
            flushMessagesToDB(clsId, grpId)
        }

        val chatKey = keyGenService.generateChatLogsKey(clsId, grpId)
        redisTemplate.opsForList().rightPush(chatKey, jacksonObjectMapper().writeValueAsString(messageDto))
        redisTemplate.opsForList().trim(chatKey, -100, -1)
    }

    override fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto) : EnterThreadResponseDto {
        val userId = enterThreadRequestDto.userId
        val clsId = enterThreadRequestDto.clsId
        val grpId = UUID.fromString(enterThreadRequestDto.grpId)
        val userDiv = enterThreadRequestDto.userDiv

        logger.info("$userId, $clsId, $grpId, $userDiv")

        if(verifyUser(userId, grpId, userDiv, clsId)){
            return EnterThreadResponseDto(
                statusCode = "VERIFIED",
                statusToken = grpMemRepo.findGrpMemByUserIdAndGrpId(userId, grpId)?.id.toString(),
            )
        }else{
            throw IllegalArgumentException("Not valid User")
        }


    }

    override fun restoreThread(enterThreadRequestDto: RestoreThreadRequestDto): RestoreThreadResponseDto {
        val redisKey = keyGenService.generateChatLogsKey(enterThreadRequestDto.clsId, enterThreadRequestDto.grpId)

        val messages = redisTemplate.opsForList().range(redisKey, 0, -1) ?: emptyList()

        return RestoreThreadResponseDto(
            userId = enterThreadRequestDto.userId,
            clsId = enterThreadRequestDto.clsId,
            grpId = enterThreadRequestDto.grpId,
            messages = messages
        )
    }


    @Scheduled(fixedRate = 30000)
    fun flushAllPendingMessages() {
        // 락 획득 시도 (SETNX)
        val lockAcquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCKED", Duration.ofSeconds(LOCK_TTL.toLong()))

        if (lockAcquired == false) {
            logger.info("🚫 다른 프로세스가 실행 중이므로 종료")
            return // 다른 인스턴스가 실행 중이면 중복 실행 방지
        }

        try {
            // ✅ 2️⃣ Redis에서 모든 대기 메시지 키 가져오기
            val sessionKeys = redisTemplate.keys("pending_messages:*:*")

            sessionKeys.forEach { redisKey ->
                val keys = redisKey.split(":")
                if (keys.size < 3) return@forEach

                val clsId = keys[1]
                val grpId = keys[2]

                flushMessagesToDB(clsId, grpId)
            }
        } catch (e: Exception) {
            logger.error("❌ 메시지 플러시 중 오류 발생: ", e)
        } finally {
            // ✅ 3️⃣ 락 해제
            redisTemplate.delete(LOCK_KEY)
        }
    }

    fun flushMessagesToDB(clsId: String, grpId: String) {
        val redisKey = keyGenService.generatePendingMessagesKey(clsId, grpId)

        val messages = redisTemplate.opsForList().range(redisKey, 0, -1) ?: emptyList()
        if (messages.isEmpty()) return

        val bulkMessages = messages.map { json ->
            jacksonObjectMapper().readValue(json, MessageDto::class.java)
        }.map { msg ->
            DiscThreadHist(
                id = UUID.randomUUID(),
                clsId = clsId,
                grpId = UUID.fromString(grpId),
                userId = msg.sender,
                msg = msg.message,
                insDt = Instant
                    .parse(msg.timestamp)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime()
            )
        }

        logger.info("📝 DB저장 완료: $clsId, $grpId, ${bulkMessages.size}개")

        discThreadHistRepository.saveAll(bulkMessages)

        // 3️⃣ Redis에서 처리된 메시지 삭제
        redisTemplate.delete(redisKey)
    }




    private fun verifyUser(userId: String, grpId : UUID, userDiv : String, clsId: String) : Boolean {
        return if(userDiv == "O10") {
            if(clsRepo.isUserOwner(clsId, userId)){
                return true
            }else{
                throw IllegalArgumentException("Not Owner for this class")
            }
        }else{
            grpMemRepo.findGrpMemByUserId(userId, grpId) ?: false
        }
    }
}
