
package org.eduai.educhat.service.discussion.impl


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.eduai.educhat.common.enum.DiscussionStatus
import org.eduai.educhat.dto.discussion.request.*
import org.eduai.educhat.dto.message.MessageDto
import org.eduai.educhat.dto.message.request.SendMessageRequestDto
import org.eduai.educhat.dto.discussion.response.EnterThreadResponseDto
import org.eduai.educhat.dto.discussion.response.RestoreThreadResponseDto
import org.eduai.educhat.dto.discussion.response.SearchResponseDto
import org.eduai.educhat.dto.discussion.response.ThreadAccessResponseDto
import org.eduai.educhat.dto.message.SearchResultMessageDto
import org.eduai.educhat.entity.DiscThreadHist
import org.eduai.educhat.repository.ClsMstRepository
import org.eduai.educhat.repository.DiscGrpMemRepository
import org.eduai.educhat.repository.DiscGrpRepository
import org.eduai.educhat.repository.DiscThreadHistRepository
import org.eduai.educhat.service.discussion.ChannelManageService
import org.eduai.educhat.service.KeyGeneratorService
import org.eduai.educhat.service.discussion.ThreadManageService
import org.eduai.educhat.service.discussion.ThreadSessionManager
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.*
import java.util.*

@Service
class ThreadManageServiceImpl(
    private val redisTemplate: StringRedisTemplate,
    private val channelManageService: ChannelManageService,
    private val keyGenService: KeyGeneratorService,
    private val clsRepo: ClsMstRepository,
    private val grpRepo: DiscGrpRepository,
    private val grpMemRepo: DiscGrpMemRepository,
    private val discThreadHistRepository: DiscThreadHistRepository,
    private val threadSessionManager: ThreadSessionManager
) : ThreadManageService {


    companion object {
        private val logger = LoggerFactory.getLogger(ThreadManageServiceImpl::class.java)
        private const val LOCK_KEY = "message_flush_lock"
        private const val LOCK_TTL = 10
        private const val PAGE_SIZE = 100
        val seoulZoneId = ZoneId.of("Asia/Seoul")
    }

    override fun createGroupChannel(clsId: String, groupId: UUID) {
        val grpId = groupId.toString()
        val sessionListKey = keyGenService.generateRedisSessionKey(clsId)
        val sessionGrpKey = keyGenService.generateRedisSessionHashKey(groupId.toString())
        val topicName = generateTopicName(clsId, grpId)

        channelManageService.createGroupChannel(topicName)
        redisTemplate.opsForHash<String, String>().put(sessionListKey, sessionGrpKey, topicName)

        logger.info("채팅방 생성 완료: $topicName (Group ID: $groupId)")
    }

    override fun removeGroupChannel(clsId: String, groupId: UUID) {
        val grpId = groupId.toString()
        val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(grpId)
        val topicName = generateTopicName(clsId, grpId)

        channelManageService.removeGroupChannel(topicName)
        redisTemplate.opsForHash<String, String>().delete(clsSessionKey, grpSessionKey)

        flushMessagesToDB(clsId, grpId)

        logger.info("채팅방 삭제 완료: (Group ID: $groupId)")
    }

    override fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto) {
        val clsId = sendMessageRequestDto.clsId
        val grpId = sendMessageRequestDto.grpId

        val topicName = generateTopicName(clsId, grpId)

        val messageDto = MessageDto(
            clsId = clsId,
            sender = sendMessageRequestDto.sender,
            senderName = sendMessageRequestDto.senderName,
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

    override fun restoreThread(restoreRequest: RestoreThreadRequestDto): RestoreThreadResponseDto {
        val clsId = restoreRequest.clsId
        val grpIdStr = restoreRequest.grpId // UUID 파싱 전 문자열
        val grpId = UUID.fromString(grpIdStr)
        val lastTimestampStr = restoreRequest.lastMessageTimestamp
        val objectMapper = jacksonObjectMapper()

        if (lastTimestampStr == null) {
            logger.info("📜 최초 채팅 기록 로딩 요청 (Redis 캐시 확인): clsId=$clsId, grpId=$grpIdStr")
            val redisKey = keyGenService.generateChatLogsKey(clsId, grpIdStr)
            val cachedMessagesJson = redisTemplate.opsForList().range(redisKey, -PAGE_SIZE.toLong(), -1)

            if (!cachedMessagesJson.isNullOrEmpty()) {
                logger.info("✅ Redis 캐시 히트: ${cachedMessagesJson.size}개 메시지 발견")
                try {
                    val cachedMessagesDto = cachedMessagesJson.map { json ->
                        objectMapper.readValue<MessageDto>(json)
                    }
                    val hasNext = cachedMessagesDto.size == PAGE_SIZE

                    logger.info("📜 Redis 캐시에서 ${cachedMessagesDto.size}개 로드 완료, 다음 페이지 유무: $hasNext")

                    return RestoreThreadResponseDto(
                        messages = cachedMessagesDto,
                        hasNext = hasNext
                    )
                } catch (e: Exception) {
                    logger.error("🚨 Redis 캐시 메시지 파싱 오류 (DB 조회로 대체): ", e)
                }
            } else {
                logger.info("ℹ️ Redis 캐시 미스 또는 비어있음. DB 조회 시작.")
            }

            val pageable: Pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("insDt").descending())
            val messagePage = discThreadHistRepository.findByClsIdAndGrpIdOrderByInsDtDesc(clsId, grpId, pageable)
            val messagesDtoList = messagePage.content.map { hist ->
                MessageDto(
                    clsId = hist.clsId, grpId = hist.grpId.toString(), sender = hist.userId,
                    senderName = hist.userName, message = hist.msg,
                    timestamp = hist.insDt.atZone(seoulZoneId).toInstant().toString()
                )
            }
            logger.info("📜 DB에서 ${messagesDtoList.size}개 로드 완료, 다음 페이지 유무: ${messagePage.hasNext()}")
            return RestoreThreadResponseDto(
                messages = messagesDtoList, hasNext = messagePage.hasNext()
            )

        } else {
            logger.info("📜 이전 채팅 기록 로딩 요청 (DB 조회): clsId=$clsId, grpId=$grpIdStr, before=$lastTimestampStr")
            val pageable: Pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("insDt").descending())
            try {
                val lastTimestamp = Instant.parse(lastTimestampStr).atZone(seoulZoneId).toLocalDateTime()
                val messagePage = discThreadHistRepository.findByClsIdAndGrpIdAndInsDtBeforeOrderByInsDtDesc(clsId, grpId, lastTimestamp, pageable)
                val messagesDtoList = messagePage.content.map { hist ->
                    MessageDto(
                        clsId = hist.clsId, grpId = hist.grpId.toString(), sender = hist.userId,
                        senderName = hist.userName, message = hist.msg,
                        timestamp = hist.insDt.atZone(seoulZoneId).toInstant().toString()
                    )
                }
                logger.info("📜 DB에서 이전 메시지 ${messagesDtoList.size}개 로드 완료, 다음 페이지 유무: ${messagePage.hasNext()}")
                return RestoreThreadResponseDto(
                    messages = messagesDtoList.reversed(), hasNext = messagePage.hasNext()
                )
            } catch (e: Exception) {
                logger.error("🚨 잘못된 타임스탬프 형식 또는 이전 메시지 DB 조회 오류: $lastTimestampStr", e)
                return RestoreThreadResponseDto(
                    messages = emptyList(), hasNext = false
                )
            }
        }
    }

    override fun pauseThread(pauseRequest: PauseThreadRequestDto): String {
        if (verifyUser(pauseRequest.userId, UUID.fromString(pauseRequest.grpId), "O10", pauseRequest.clsId)) {
            val updateResult = grpRepo.updateGrpStatus(UUID.fromString(pauseRequest.grpId), DiscussionStatus.PAU.toString(), LocalDateTime.now())
            when (updateResult) {
                1 -> {
                    threadSessionManager.disconnectThreadSessions(pauseRequest.grpId)
                    logger.info("스레드(threadId: ${pauseRequest.grpId})의 모든 세션을 강제 종료했습니다.")
                    return "success"
                }
                0 -> {
                    throw IllegalArgumentException("스레드가 존재하지 않습니다.")
                }
                else -> {
                    throw IllegalArgumentException("중복된 스레드가 존재합니다. 관리자에게 문의하세요.")
                }
            }
        } else {
            throw IllegalArgumentException("Not Owner for this class")
        }
    }

    override fun restartThread(restartRequest: RestartThreadRequestDto) : String {
        if(verifyUser(restartRequest.userId, UUID.fromString(restartRequest.grpId), "O10", restartRequest.clsId)){
            val updateResult = grpRepo.updateGrpStatus(UUID.fromString(restartRequest.grpId), DiscussionStatus.ACT.toString(), LocalDateTime.now())
            when (updateResult) {
                1 -> {
                    return "success"
                }
                0 -> {
                    throw IllegalArgumentException("채팅방이 존재하지 않습니다.")
                }
                else -> {
                    throw IllegalArgumentException("중복된 채팅방이 존재합니다. 관리자에게 문의하세요.")
                }
            }
        }else{
            throw IllegalArgumentException("Not Owner for this class")
        }
    }

    override fun checkAccess(threadAccessRequestDto: ThreadAccessRequestDto): ThreadAccessResponseDto {
        return ThreadAccessResponseDto(
            verifyResult = verifyUser(
                threadAccessRequestDto.userId,
                UUID.fromString(threadAccessRequestDto.grpId),
                threadAccessRequestDto.userDiv,
                threadAccessRequestDto.clsId
            ),
            isActive = grpRepo.findByGrpId(UUID.fromString(threadAccessRequestDto.grpId)).isActive,
        )
    }

    override fun searchOnThread(searchRequestDto: SearchRequestDto): SearchResponseDto {
        val searchResult = discThreadHistRepository.searchMsgBySearchTerm(
            UUID.fromString(searchRequestDto.grpId),
            searchRequestDto.searchTerm
        )

        val resultList = searchResult.map { hist ->
            SearchResultMessageDto(
                msgId = hist.id,
                senderId = hist.userId,
                senderName = hist.userName, message = hist.msg,
                timestamp = hist.insDt.atZone(seoulZoneId).toInstant().toString()
            )
        }

        return SearchResponseDto(msgResult = resultList, userResult = resultList)
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
                clsId = clsId,
                grpId = UUID.fromString(grpId),
                userId = msg.sender,
                userName = msg.senderName,
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

    private fun generateTopicName(clsId:String, grpId:String) : String{

        val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(grpId)

        return redisTemplate.opsForHash<String, String>().get(clsSessionKey, grpSessionKey)
            ?: throw IllegalArgumentException("유효한 채널이 아님")
    }

    override fun addTestMessages(request: AddTestMessagesRequestDto) {
        val clsId = request.clsId
        val grpIdStr = request.grpId
        val grpIdUUID = UUID.fromString(grpIdStr)
        val count = request.count
        val senderId = request.senderId
        val senderName = request.senderName
        val seoulZoneId = ZoneId.of("Asia/Seoul")
        val objectMapper = jacksonObjectMapper()

        logger.info("🧪 테스트 메시지 생성 시작: clsId=$clsId, grpId=$grpIdStr, count=$count")

        val messagesToSave = mutableListOf<DiscThreadHist>()
        val messageDtosForCache = mutableListOf<MessageDto>()
        val startTime = Instant.now().atZone(seoulZoneId)

        for (i in 0 until count) {
            // 순서 보장을 위해 현재 시간부터 과거로 2초씩 간격을 둠 (i=0이 가장 최신)
            val messageTimestamp = startTime.minusSeconds(((count - 1 - i) * 2).toLong())
            val messageContent = "테스트 메시지 ${i + 1} at ${messageTimestamp}"

            // DB 저장을 위한 Entity 생성
            val historyEntry = DiscThreadHist(
                clsId = clsId,
                grpId = grpIdUUID,
                userId = senderId,
                userName = senderName,
                msg = messageContent,
                insDt = messageTimestamp.toLocalDateTime()
            )
            messagesToSave.add(historyEntry)

            // Redis 캐시용 DTO 생성
            val messageDto = MessageDto(
                clsId = clsId,
                grpId = grpIdStr,
                sender = senderId,
                senderName = senderName,
                message = messageContent,
                timestamp = messageTimestamp.toString() // UTC ISO 8601 문자열
            )
            messageDtosForCache.add(messageDto)
        }

        discThreadHistRepository.saveAll(messagesToSave)
        logger.info("🧪 DB 저장 완료: ${messagesToSave.size}개")

        try {
            val chatKey = keyGenService.generateChatLogsKey(clsId, grpIdStr)
            val messageJsonList = messageDtosForCache.map { objectMapper.writeValueAsString(it) }

            // 새로 생성된 메시지를 캐시에 추가 (오른쪽에 추가)
            redisTemplate.opsForList().rightPushAll(chatKey, messageJsonList)
            // 전체 캐시 크기를 PAGE_SIZE로 Trim
            redisTemplate.opsForList().trim(chatKey, -PAGE_SIZE.toLong(), -1)

            logger.info("🧪 Redis 캐시 업데이트 완료: $chatKey (최대 $PAGE_SIZE 개 유지)")
        } catch (e: Exception) {
            logger.error("🚨 테스트 메시지 Redis 캐시 업데이트 중 오류 발생: ", e)
            // 캐싱 실패해도 DB 저장은 완료된 상태
        }

        logger.info("🧪 테스트 메시지 생성 완료")
    }

}
