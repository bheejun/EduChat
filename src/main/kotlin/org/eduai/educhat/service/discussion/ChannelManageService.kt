package org.eduai.educhat.service.discussion

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import org.eduai.educhat.dto.message.MessageDto
import org.eduai.educhat.repository.DiscThreadHistRepository
import org.eduai.educhat.service.CustomRedisMessageListener
import org.eduai.educhat.service.KeyGeneratorService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.Subscription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.InetAddress
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ChannelManageService(
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val redisTemplate: StringRedisTemplate,
    private val customRedisMessageListener: CustomRedisMessageListener,
    private val keyGenService: KeyGeneratorService,
    private val discThreadHistRepository: DiscThreadHistRepository,
    private val streamContainer: StreamMessageListenerContainer<String, MapRecord<String, String, String>>
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChannelManageService::class.java)
        private const val CONSUMER_GROUP = "flushGroup"
    }
    private val activeStreamSubscriptions = ConcurrentHashMap<String, Subscription>()


    @PostConstruct
    fun restoreChannelsFromRedis() {
        // Redis 연결 상태 확인
        logger.info("Redis 연결 상태: {}", redisTemplate.connectionFactory?.connection?.isClosed != true)

        val sessionKeys = redisTemplate.keys("cls_sessions:*") // ✅ 모든 채널 키 조회
        sessionKeys.forEach { sessionKey ->
            val existingChannels = redisTemplate.opsForHash<String, String>().entries(sessionKey)
            existingChannels.forEach { (sessionHashKey, topicName) ->
                val grpId = keyGenService.decodeRedisSessionHashKey(sessionHashKey)
                val clsId = keyGenService.decodeRedisSessionKey(sessionKey)
                val topic = ChannelTopic(topicName)
                val streamKey = keyGenService.generateStreamKey(clsId, grpId)

                // ✅ Redis Pub/Sub 채널 복원
                redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
                logger.info("🔄 Redis에서 기존 채팅방 복원: $topicName (Group ID: $grpId)")

                // ✅ 컨슈머 그룹 생성
                createConsumerGroupIfAbsent(streamKey)

                // ✅ 기본 스트림 리스너 등록
                registerStreamListener(streamKey)

                // ✅ 최신 100개 메시지 캐싱
                cacheRecentMessages(clsId, grpId)
            }
        }
    }

    fun createGroupChannel(topicName: String) {
        val topic = ChannelTopic(topicName)
        redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
        logger.info("✅ 채팅방 생성 및 구독 등록: $topicName")
    }

    fun removePubSubListener(topicName: String) {
        val topic = ChannelTopic(topicName)
        redisMessageListenerContainer.removeMessageListener(customRedisMessageListener, topic)
        logger.info("🛑 채팅방 구독 해제됨: $topicName")
    }

    fun createConsumerGroupIfAbsent(streamKey: String) {
        try {
            redisTemplate.connectionFactory?.connection?.use { connection ->
                logger.info("🔧 스트림 [$streamKey]에 Consumer Group [$CONSUMER_GROUP] 생성 시도 (Offset: 0-0, MkStream: true)...")

                connection.streamCommands().xGroupCreate(
                    streamKey.toByteArray(Charsets.UTF_8),
                    CONSUMER_GROUP,
                    ReadOffset.from("0-0"),
                    true
                )
                logger.info("✅ 스트림 [$streamKey]에 Consumer Group [$CONSUMER_GROUP] 생성 완료.")
            } ?: logger.error("Redis Connection Factory is null. Consumer Group 생성 불가.")

        } catch (e: RedisSystemException) {
            if (e.cause?.message?.contains("BUSYGROUP") == true) {
                logger.warn("⚠️ Consumer Group [$CONSUMER_GROUP]은(는) 스트림 [$streamKey]에 이미 존재합니다.")
            } else {
                logger.error("❌ 스트림 [$streamKey]에 Consumer Group [$CONSUMER_GROUP] 생성 중 Redis 오류 발생.", e)
            }
        } catch (e: Exception) {
            logger.error("❌ 스트림 [$streamKey]에 Consumer Group [$CONSUMER_GROUP] 생성 중 예외 발생.", e)
        }
    }

    fun registerStreamListener(streamKey: String) {
        activeStreamSubscriptions.compute(streamKey) { _, existingSubscription ->
            // 기존 구독이 활성 상태면 취소
            if (existingSubscription != null && existingSubscription.isActive) {
                logger.warn("기존 활성 Stream Listener Subscription 취소 시도 for $streamKey")
                try { existingSubscription.cancel() } catch (e: Exception) { logger.error("기존 Subscription 취소 중 오류 for $streamKey", e) }
            } else if (existingSubscription != null) {
                logger.info("기존 비활성 Stream Listener Subscription 발견 for $streamKey")
            }

            // 고유한 컨슈머 이름 생성 (동일 호스트에서 여러 인스턴스 실행 가능성 고려)
            val consumerName = "${InetAddress.getLocalHost().hostName}-${UUID.randomUUID().toString().substring(0, 8)}"
            val consumer = Consumer.from(CONSUMER_GROUP, consumerName)
            // 그룹 내 마지막으로 처리된 메시지부터 읽기 시작
            val offset = StreamOffset.create(streamKey, ReadOffset.lastConsumed())

            logger.info("Stream Listener 등록 시도 for $streamKey with Consumer $consumerName")

            // receiveAutoAck 사용 및 반환된 Subscription 저장
            val newSubscription = streamContainer.receiveAutoAck(consumer, offset) { record ->
                try {
                    val map = record.value
                    // Null 처리 강화 및 기본값 사용
                    val dto = MessageDto(
                        msgId      = map["msgId"] ?: UUID.randomUUID().toString(),
                        clsId      = map["clsId"] ?: "UNKNOWN_CLS",
                        grpId      = map["grpId"] ?: "UNKNOWN_GRP",
                        sender     = map["sender"] ?: "UNKNOWN_SENDER",
                        senderName = map["senderName"] ?: "Unknown User",
                        message    = map["message"] ?: "",
                        timestamp  = map["timestamp"] ?: Instant.now().toString()
                    )
                    discThreadHistRepository.save(dto.messageDtoToEntity())
                    logger.debug("✅ Stream AutoAck DB 저장 완료 [$streamKey] by [$consumerName]: MsgId=${dto.msgId}") // 로그 레벨 조정

                } catch (e: Exception) {
                    logger.error("🚨 스트림 메시지 처리/저장 중 오류 [$streamKey] by [$consumerName]: RecordId=${record.id.value}", e)
                }
            }

            if (newSubscription != null) {
                logger.info("✅ Stream Listener Subscription 등록 완료 for $streamKey (Consumer: $consumerName)")
                newSubscription // 맵에 저장될 새 Subscription 객체
            } else {
                logger.error("🚨 Stream Listener Subscription 등록 실패 for $streamKey")
                null // 기존 값 유지 또는 null 저장
            }
        }
    }


    fun stopStreamListener(streamKey: String) {
        activeStreamSubscriptions.remove(streamKey)?.let { subscription ->
            if (subscription.isActive) {
                try {
                    subscription.cancel()
                    logger.info("✅ Stream Listener Subscription 취소 완료 for $streamKey")
                } catch (e: Exception) {
                    logger.error("🚨 Stream Listener Subscription 취소 중 오류 for $streamKey", e)
                }
            } else {
                logger.warn("ℹ️ Stream Listener Subscription for $streamKey was already inactive.")
            }
        } ?: logger.info("ℹ️ 중지할 Stream Listener Subscription 없음 for $streamKey.")
        // Consumer 삭제 로직은 필요 시 추가 (XGROUP DELCONSUMER)
    }



    private fun cacheRecentMessages(clsId: String, grpId: String) {
        val redisKey = keyGenService.generateChatLogsKey(clsId, grpId)
        redisTemplate.delete(redisKey)
        logger.info("🔄 기존 Redis 캐시 삭제: $redisKey")

        val recentMessages = discThreadHistRepository.findTop100ByClsIdAndGrpIdOrderByInsDtDesc(clsId, UUID.fromString(grpId))

        if (recentMessages.isNotEmpty()) {
            val objectMapper = jacksonObjectMapper()
            val seoulZoneId = ZoneId.of("Asia/Seoul")

            val messageJsonList = recentMessages.map { msg ->
                val msgDto = MessageDto(
                    msgId = msg.id.toString(),
                    clsId = clsId,
                    sender = msg.userId,
                    senderName = msg.userName,
                    grpId = grpId,
                    message = msg.msg,
                    timestamp = msg.insDt.atZone(seoulZoneId).toInstant().toString()
                )
                objectMapper.writeValueAsString(msgDto)
            }

            redisTemplate.opsForList().leftPushAll(redisKey, messageJsonList)

            logger.info("📝 Redis에 최신 ${recentMessages.size}개 메시지 캐싱 완료 (기존 캐시 삭제 후): $grpId")
        } else {
            logger.info("ℹ️ DB에 캐싱할 최신 메시지가 없음: $clsId, $grpId")
        }
    }
}