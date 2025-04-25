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
import org.springframework.data.redis.connection.stream.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        private const val PAGE_SIZE = 100
        val seoulZoneId: ZoneId = ZoneId.of("Asia/Seoul")
    }

    override fun createGroupChannel(clsId: String, groupId: UUID) {
        val grpId = groupId.toString()
        val sessionListKey = keyGenService.generateRedisSessionKey(clsId)
        val sessionGrpKey = keyGenService.generateRedisSessionHashKey(groupId.toString())
        val streamKey = keyGenService.generateStreamKey(clsId, grpId)
        val topicName = generateTopicName(grpId)

        channelManageService.createGroupChannel(topicName)
        channelManageService.createConsumerGroupIfAbsent(streamKey)
        channelManageService.registerStreamListener(streamKey)

        redisTemplate.opsForHash<String, String>().put(sessionListKey, sessionGrpKey, topicName)

        logger.info("채팅방 생성 완료: $topicName (Group ID: $groupId)")
    }


    @Transactional
    override fun removeGroupChannel(clsId: String, groupId: UUID) {
        val grpId = groupId.toString()
        val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
        val grpSessionKey = keyGenService.generateRedisSessionHashKey(grpId)
        val chatLogKey = keyGenService.generateChatLogsKey(clsId, grpId)
        val topicName = generateTopicName(grpId)

        channelManageService.removePubSubListener(topicName)
        redisTemplate.opsForHash<String, String>().delete(clsSessionKey, grpSessionKey)

        //TODO:채팅 로그 DB에 다 저장 되면 삭제
        redisTemplate.delete(chatLogKey)

        logger.info("채팅방 삭제 완료: (Group ID: $groupId)")
    }

    @Transactional
    override fun closeThread(closeRequestDto: PauseThreadRequestDto): String {
        val clsId =closeRequestDto.clsId
        val grpId = UUID.fromString(closeRequestDto.grpId)
        val grpIdStr = closeRequestDto.grpId
        if (verifyUser(closeRequestDto.userId, grpId, "O10", clsId)) {
            val updateResult = grpRepo.updateGrpStatus(
                UUID.fromString(closeRequestDto.grpId),
                DiscussionStatus.FIN.toString(),
                LocalDateTime.now()
            )
            val streamKey = keyGenService.generateStreamKey(clsId, grpIdStr)
            val topicName = generateTopicName(grpIdStr)
            val chatLogKey = keyGenService.generateChatLogsKey(clsId, grpIdStr)
            val clsSessionKey = keyGenService.generateRedisSessionKey(clsId)
            val grpSessionKey= keyGenService.generateRedisSessionHashKey(grpIdStr)

            try {

                threadSessionManager.disconnectThreadSessions(grpIdStr, DiscussionStatus.FIN.toString())
                logger.info("WebSocket 세션 강제 종료 완료.")

                // 1. <<< 스트림 리스너 중지 >>>
                channelManageService.stopStreamListener(streamKey)
                logger.info("자동 Stream Listener 중지 완료: $streamKey")

                // 2. Redis Stream의 모든 메시지를 DB에 영속화 (수동 처리)
                persistAllMessagesFromStream(streamKey, clsId, grpId) // 이전 답변에서 제안된 헬퍼 함수
                logger.info("Redis Stream 메시지 DB 영속화 완료: $streamKey")

                // 3. Redis 리소스 정리
                logger.info("Redis 리소스 정리 시작...")
                // 3a. Pub/Sub 리스너 해제
                channelManageService.removePubSubListener(topicName) // 이름 변경된 메소드 호출

                // 3b. Redis Stream 삭제
                redisTemplate.delete(streamKey)
                logger.info("Redis Stream 삭제 완료: $streamKey")

                // 3c. Redis List 캐시 삭제
                redisTemplate.delete(chatLogKey)
                logger.info("Redis List 캐시 삭제 완료: $chatLogKey")

                // 3d. Redis Hash 세션 정보 삭제
                redisTemplate.opsForHash<String, String>().delete(clsSessionKey, grpSessionKey)
                logger.info("Redis Hash 세션 정보 삭제 완료...")

                logger.info("스레드 종료 및 관련 리소스 정리 완료.")
                return checkResultAndDisconnectSession(updateResult, closeRequestDto.grpId)

            } catch (e: Exception) {
                logger.error("🚨 스레드 종료 처리 중 오류 발생 (영속화 또는 리소스 정리 실패)", e)
                throw RuntimeException("스레드 종료 처리 중 오류 발생", e)
            }
        } else {
            throw IllegalArgumentException("Not Owner for this class")
        }
    }



    @Transactional
    override fun sendMessageToRedis(sendMessageRequestDto: SendMessageRequestDto) {
        val clsId = sendMessageRequestDto.clsId
        val grpId = sendMessageRequestDto.grpId

        val topicName = generateTopicName(grpId)

        val messageDto = MessageDto(
            msgId = UUID.randomUUID().toString(),
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
        val streamKey = keyGenService.generateStreamKey(clsId, grpId)
        val record = MapRecord.create(
            streamKey,
            mapOf(
                "msgId"      to messageDto.msgId,
                "clsId"      to messageDto.clsId,
                "grpId"      to messageDto.grpId,
                "sender"     to messageDto.sender,
                "senderName" to messageDto.senderName,
                "message"    to messageDto.message,
                "timestamp"  to messageDto.timestamp
            )
        )

        try {
            val recordId = redisTemplate.opsForStream<String, String>().add(record)
            logger.info("✅ Stream 저장 완료: Key=$streamKey, RecordId=$recordId, MsgId=${messageDto.msgId}")
        } catch (e: Exception) {
            logger.error("🚨 Stream 저장 실패: Key=$streamKey, MsgId=${messageDto.msgId}", e)
        }


        val chatKey = keyGenService.generateChatLogsKey(clsId, grpId)
        redisTemplate.opsForList().rightPush(chatKey, jacksonObjectMapper().writeValueAsString(messageDto))
        redisTemplate.opsForList().trim(chatKey, -100, -1)
    }

    override fun enterChannel(enterThreadRequestDto: EnterThreadRequestDto): EnterThreadResponseDto {
        val userId = enterThreadRequestDto.userId
        val clsId = enterThreadRequestDto.clsId
        val grpId = UUID.fromString(enterThreadRequestDto.grpId)
        val userDiv = enterThreadRequestDto.userDiv

        logger.info("$userId, $clsId, $grpId, $userDiv")

        if (verifyUser(userId, grpId, userDiv, clsId)) {
            return EnterThreadResponseDto(
                statusCode = "VERIFIED",
                statusToken = grpMemRepo.findGrpMemByUserIdAndGrpId(userId, grpId)?.id.toString(),
            )
        } else {
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

                    logger.info("📜 Redis 캐시 에서 ${cachedMessagesDto.size}개 로드 완료, 다음 페이지 유무: $hasNext")

                    return RestoreThreadResponseDto(
                        messages = cachedMessagesDto,
                        hasNext = hasNext
                    )
                } catch (e: Exception) {
                    logger.error("🚨 Redis 캐시 메시지 파싱 오류 (DB 조회로 대체): ", e)
                }
            } else {
                logger.info("ℹ️ Redis 캐시 미스 또는 비어 있음. DB 조회 시작.")
            }

            val pageable: Pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("insDt").descending())
            val messagePage = discThreadHistRepository.findByClsIdAndGrpIdOrderByInsDtDesc(clsId, grpId, pageable)
            val messagesDtoList = messagePage.content.map { hist ->
                MessageDto(
                    msgId = hist.id.toString(),
                    clsId = hist.clsId, grpId = hist.grpId.toString(), sender = hist.userId,
                    senderName = hist.userName, message = hist.msg,
                    timestamp = hist.insDt.atZone(seoulZoneId).toInstant().toString()
                )
            }
            logger.info("📜 DB 에서 ${messagesDtoList.size}개 로드 완료, 다음 페이지 유무: ${messagePage.hasNext()}")
            return RestoreThreadResponseDto(
                messages = messagesDtoList, hasNext = messagePage.hasNext()
            )

        } else {
            logger.info("📜 이전 채팅 기록 로딩 요청 (DB 조회): clsId=$clsId, grpId=$grpIdStr, before=$lastTimestampStr")
            val pageable: Pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("insDt").descending())
            try {
                val lastTimestamp = Instant.parse(lastTimestampStr).atZone(seoulZoneId).toLocalDateTime()
                val messagePage = discThreadHistRepository.findByClsIdAndGrpIdAndInsDtBeforeOrderByInsDtDesc(
                    clsId,
                    grpId,
                    lastTimestamp,
                    pageable
                )
                val messagesDtoList = messagePage.content.map { hist ->
                    MessageDto(
                        msgId = hist.id.toString(),
                        clsId = hist.clsId, grpId = hist.grpId.toString(), sender = hist.userId,
                        senderName = hist.userName, message = hist.msg,
                        timestamp = hist.insDt.atZone(seoulZoneId).toInstant().toString()
                    )
                }
                logger.info("📜 DB 에서 이전 메시지 ${messagesDtoList.size}개 로드 완료, 다음 페이지 유무: ${messagePage.hasNext()}")
                return RestoreThreadResponseDto(
                    messages = messagesDtoList.reversed(), hasNext = messagePage.hasNext()
                )
            } catch (e: Exception) {
                logger.error("🚨 잘못된 타임 스탬프 형식 또는 이전 메시지 DB 조회 오류: $lastTimestampStr", e)
                return RestoreThreadResponseDto(
                    messages = emptyList(), hasNext = false
                )
            }
        }
    }

    override fun pauseThread(pauseRequest: PauseThreadRequestDto): String {
        if (verifyUser(pauseRequest.userId, UUID.fromString(pauseRequest.grpId), "O10", pauseRequest.clsId)) {
            val updateResult = grpRepo.updateGrpStatus(
                UUID.fromString(pauseRequest.grpId),
                DiscussionStatus.PAU.toString(),
                LocalDateTime.now()
            )
            return checkResultAndDisconnectSession(updateResult, pauseRequest.grpId)
        } else {
            throw IllegalArgumentException("Not Owner for this class")
        }
    }

    override fun restartThread(restartRequest: RestartThreadRequestDto): String {
        if (verifyUser(restartRequest.userId, UUID.fromString(restartRequest.grpId), "O10", restartRequest.clsId)) {
            val updateResult = grpRepo.updateGrpStatus(
                UUID.fromString(restartRequest.grpId),
                DiscussionStatus.ACT.toString(),
                LocalDateTime.now()
            )
            when (updateResult) {
                1 -> {
                    return "success"
                }

                0 -> {
                    throw IllegalArgumentException("채팅 방이 존재 하지 않음.")
                }

                else -> {
                    throw IllegalArgumentException("중복된 채팅 방이 존재.")
                }
            }
        } else {
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

        val userResult = discThreadHistRepository.searchUserBySearchTerm(
            UUID.fromString(searchRequestDto.grpId),
            searchRequestDto.searchTerm
        )

        val searchResultList = searchResult.map { hist ->
            SearchResultMessageDto(
                msgId = hist.id ,
                senderId = hist.userId,
                senderName = hist.userName, message = hist.msg,
                timestamp = hist.insDt.atZone(seoulZoneId).toInstant().toString()
            )
        }

        val userResultList = userResult.map { hist ->
            SearchResultMessageDto(
                msgId = hist.id ,
                senderId = hist.userId,
                senderName = hist.userName, message = hist.msg,
                timestamp = hist.insDt.atZone(seoulZoneId).toInstant().toString()
            )
        }

        return SearchResponseDto(msgResult = searchResultList, userResult = userResultList)
    }

    private fun verifyUser(userId: String, grpId: UUID, userDiv: String, clsId: String): Boolean {
        return if (userDiv == "O10") {
            if (clsRepo.isUserOwner(clsId, userId)) {
                return true
            } else {
                throw IllegalArgumentException("Not Owner for this class")
            }
        } else {
            grpMemRepo.findGrpMemByUserId(userId, grpId) ?: false
        }
    }

    private fun generateTopicName( grpId: String): String {
        val topicName = "chat:$grpId"
        logger.debug("Generated Topic Name: {}", topicName)
        return topicName
    }

    private fun checkResultAndDisconnectSession(updateResult: Int, grpId: String): String{
        when (updateResult) {
            1 -> {
                threadSessionManager.disconnectThreadSessions(grpId, DiscussionStatus.PAU.toString())
                logger.info("스레드(threadId: ${grpId})의 모든 세션을 강제 종료했습니다.")
                return "success"
            }

            0 -> {
                throw IllegalArgumentException("스레드가 존재하지 않습니다.")
            }

            else -> {
                throw IllegalArgumentException("중복된 스레드가 존재합니다. 관리자에게 문의하세요.")
            }
        }
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
            // 순서 보장을 위해 현재 시간 부터 과거로 2초씩 간격을 둠 (i=0이 가장 최신)
            val messageTimestamp = startTime.minusSeconds(((count - 1 - i) * 2).toLong())
            val messageContent = "테스트 메시지 ${i + 1} at $messageTimestamp"
            val msgId = UUID.randomUUID()

            // DB 저장을 위한 Entity 생성
            val historyEntry = DiscThreadHist(
                id = msgId,
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
                msgId = msgId.toString(),
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

    private fun persistAllMessagesFromStream(streamKey: String, clsId: String, grpId: UUID) {
        logger.info("Redis Stream 영속화 시작: $streamKey")
        val messagesToSaveInDb = mutableListOf<DiscThreadHist>()
        var lastProcessedId = "0-0" // Stream의 가장 처음부터 읽기 위한 시작 ID
        val readCount = 500L // 한 번에 읽을 메시지 수
        val readTimeoutSeconds = 2L // 읽기 타임아웃 (초)

        while (true) {
            try {
                val currentOffset = StreamOffset.create(streamKey, ReadOffset.from(lastProcessedId))

                val streamMessages: List<MapRecord<String, String, String>>? = redisTemplate.opsForStream<String, String>().read(
                    StreamReadOptions.empty()
                        .count(readCount)
                        .block(Duration.ofSeconds(readTimeoutSeconds)),
                    currentOffset
                )

                if (streamMessages == null) {
                    logger.warn("Stream ($streamKey) 읽기 결과가 null (Timeout 가능성). Last Processed ID: $lastProcessedId")
                    try { Thread.sleep(100) } catch (ie: InterruptedException) { Thread.currentThread().interrupt(); throw ie }
                    continue
                }

                if (streamMessages.isEmpty()) {
                    // 현재 offset 이후로 더 이상 읽을 메시지가 없으면 종료
                    logger.info("Stream ($streamKey) 에서 더 이상 읽을 메시지 없음 (Last Processed ID: $lastProcessedId).")
                    break
                }


                val batchToSave = mutableListOf<DiscThreadHist>()
                var currentBatchLastId = lastProcessedId // 현재 배치의 마지막 ID를 추적할 변수

                for (messageRecord in streamMessages) {
                    currentBatchLastId = messageRecord.id.value // <<< 중요: 처리된 마지막 메시지 ID 업데이트 >>>

                    val messageData = messageRecord.value
                    try {
                        val msgIdStr = messageData["msgId"]
                        val timestampStr = messageData["timestamp"]

                        if (msgIdStr == null || timestampStr == null) {
                            logger.warn("🚨 필수 필드 누락 (msgId or timestamp), 메시지 건너뜀: Record ID=${messageRecord.id.value}, Data=$messageData")
                            continue
                        }

                        val historyEntry = DiscThreadHist(
                            id = UUID.fromString(msgIdStr),
                            clsId = messageData["clsId"] ?: clsId,
                            grpId = messageData["grpId"]?.let { UUID.fromString(it) } ?: grpId,
                            userId = messageData["sender"] ?: "UNKNOWN_SENDER",
                            userName = messageData["senderName"] ?: "Unknown User",
                            msg = messageData["message"] ?: "",
                            insDt = Instant.parse(timestampStr).atZone(seoulZoneId).toLocalDateTime()
                        )
                        batchToSave.add(historyEntry)
                    } catch (parseEx: Exception) {
                        logger.error("🚨 Stream 메시지 파싱/변환 오류 (Record ID: ${messageRecord.id.value}, Data: $messageData): ", parseEx)
                    }
                }

                lastProcessedId = currentBatchLastId

                if (batchToSave.isNotEmpty()) {
                    messagesToSaveInDb.addAll(batchToSave)
                    logger.debug("${batchToSave.size}개 메시지 DB 저장 목록에 추가 (현재 총 ${messagesToSaveInDb.size}개)")
                }

                // 배치 저장 로직 (1000개 단위)
                if (messagesToSaveInDb.size >= 1000) {
                    logger.info("DB 저장을 위해 ${messagesToSaveInDb.size}개 메시지 준비됨...")
                    saveMessagesBatchToDb(messagesToSaveInDb)
                    messagesToSaveInDb.clear()
                }

            } catch (redisEx: Exception) {
                logger.error("🚨 Redis Stream 읽기 오류 발생 (Key: $streamKey, Last Processed ID: $lastProcessedId): ", redisEx)
                throw redisEx // 오류 전파
            }
        } // end while

        // 루프 종료 후 남은 메시지 저장
        if (messagesToSaveInDb.isNotEmpty()) {
            logger.info("DB 저장을 위해 남은 ${messagesToSaveInDb.size}개 메시지 준비됨...")
            saveMessagesBatchToDb(messagesToSaveInDb)
        }

        logger.info("Redis Stream ($streamKey) 영속화 완료.")
    }

    private fun saveMessagesBatchToDb(messages: List<DiscThreadHist>) {
        if (messages.isEmpty()) return

        try {
            // DB 저장은 중복을 고려해야 함 (이미 저장된 msgId는 제외)
            val msgIdsInBatch = messages.map { it.id }
            val existingIdsInDb = discThreadHistRepository.findIdsByMsgIds(msgIdsInBatch).toSet() // 중복 ID 조회
            val messagesToActuallySave = messages.filter { !existingIdsInDb.contains(it.id) }

            if (messagesToActuallySave.isNotEmpty()) {
                discThreadHistRepository.saveAll(messagesToActuallySave)
                logger.info("DB에 ${messagesToActuallySave.size}개 메시지 저장 완료 (중복 제외).")
            } else {
                logger.info("DB 저장 건너뜀: 모든 메시지(${messages.size}개)가 이미 DB에 존재함.")
            }
        } catch (dbEx: Exception) {
            logger.error("🚨 DB 배치 저장 중 오류 발생: ", dbEx)
            // DB 오류 시 예외 처리 필요
            throw dbEx // 일단 상위로 전파
        }
    }



}
