
package org.eduai.educhat.service.impl


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eduai.educhat.dto.MessageDto
import org.eduai.educhat.dto.request.EnterThreadRequestDto
import org.eduai.educhat.dto.request.RestoreThreadRequestDto
import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.eduai.educhat.dto.response.EnterThreadResponseDto
import org.eduai.educhat.dto.response.RestoreThreadResponseDto
import org.eduai.educhat.repository.ClsMstRepository
import org.eduai.educhat.repository.DiscGrpMemRepository
import org.eduai.educhat.service.ChannelManageService
import org.eduai.educhat.service.KeyGeneratorService
import org.eduai.educhat.service.ThreadManageService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ThreadManageServiceImpl(
    private val redisTemplate: StringRedisTemplate,
    private val channelManageService: ChannelManageService,
    private val keyGenService: KeyGeneratorService,
    private val clsRepo: ClsMstRepository,
    private val grpMemRepo: DiscGrpMemRepository,
) : ThreadManageService{


    companion object {
        private val logger = LoggerFactory.getLogger(ThreadManageServiceImpl::class.java)
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

        val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(groupId.toString())

        channelManageService.removeGroupChannel(grpSessionKey)

        redisTemplate.opsForHash<String, String>().delete(clsSessionKey, grpSessionKey)

        logger.info("채팅방 삭제 완료: (Group ID: $groupId)")
    }

    override fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto) {
        val clsId = sendMessageRequestDto.clsId
        val grpId = sendMessageRequestDto.grpId

        val clsSessionKey  = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(grpId)

        val topicName = redisTemplate.opsForHash<String, String>().get(clsSessionKey, grpSessionKey)
            ?: throw IllegalArgumentException("유효한 채널이 아님")

        logger.info(topicName)

        val messageJson = jacksonObjectMapper().writeValueAsString(MessageDto(
            clsId = clsId,
            sender = sendMessageRequestDto.sender,
            grpId = grpId,
            message = sendMessageRequestDto.message,
            timestamp = LocalDateTime.now().toString()
        ))

        saveMessageLog(clsId, grpId, messageJson)

        redisTemplate.convertAndSend(topicName, messageJson)

        logger.info("📤 Redis 전송됨: $messageJson → 채널: $topicName")
    }

    override fun saveMessageLog(clsId: String, grpId: String, messageJson: String) {
        // 현재 청크 번호를 위한 키 생성
        val currentChunkKey = keyGenService.generateCurrentChunkKey(clsId, grpId)

        // 청크 번호 가져오기 (없으면 "0"으로 시작)
        val chunkId = (redisTemplate.opsForValue().get(currentChunkKey) ?: "0").toIntOrNull() ?: 0

        // 현재 청크의 메시지 로그 키 생성
        val chunkLogKey = keyGenService.generateLogKey(clsId, grpId, chunkId)

        // 현재 청크에 저장된 메시지 개수를 확인
        val currentChunkSize = redisTemplate.opsForList().size(chunkLogKey) ?: 0

        if (currentChunkSize >= 100) {
            // 청크가 100개 이상의 메시지를 보유하면 새로운 청크 생성
            val newChunkId = chunkId + 1
            redisTemplate.opsForValue().set(currentChunkKey, newChunkId.toString())
            val newChunkLogKey = keyGenService.generateLogKey(clsId, grpId, newChunkId)
            redisTemplate.opsForList().rightPush(newChunkLogKey, messageJson)
        } else {
            // 청크에 여유가 있으면 그대로 메시지를 추가
            redisTemplate.opsForList().rightPush(chunkLogKey, messageJson)
        }
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
                //grpMem id 를 statusToken 으로 사용
                statusToken = grpMemRepo.findGrpMemByUserIdAndGrpId(userId, grpId)?.id.toString(),
            )
        }else{
            throw IllegalArgumentException("Not valid User")
        }


    }


    //여기 검증 어케할지 생각~
    override fun restoreThread(enterThreadRequestDto: RestoreThreadRequestDto): RestoreThreadResponseDto {
        val userId = enterThreadRequestDto.userId
        val clsId = enterThreadRequestDto.clsId
        val grpId = enterThreadRequestDto.grpId
        val userDiv = enterThreadRequestDto.userDiv

        logger.info("$userId, $clsId, $grpId, $userDiv")

        if (!verifyUser(userId, UUID.fromString(grpId), userDiv, clsId)) {
            throw IllegalArgumentException("Not valid User")
        }

        // 현재 청크 번호를 조회
        val currentChunkKey = keyGenService.generateCurrentChunkKey(clsId, grpId)
        val currentChunkStr = redisTemplate.opsForValue().get(currentChunkKey) ?: "0"
        val currentChunk = currentChunkStr.toIntOrNull() ?: 0

        // 각 청크에서 메시지를 가져와 모두 합치기
        val messagesList = mutableListOf<String>()
        for (chunkIndex in 0..currentChunk) {
            val logKey = keyGenService.generateLogKey(clsId, grpId, chunkIndex)
            val chunkMessages = redisTemplate.opsForList().range(logKey, 0, -1)
            if (!chunkMessages.isNullOrEmpty()) {
                messagesList.addAll(chunkMessages)
            }
        }
        if (messagesList.isEmpty()) {
            messagesList.add("Empty Session")
        }

        return RestoreThreadResponseDto(
            userId = userId,
            clsId = clsId,
            grpId = grpId,
            messages = messagesList
        )
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
