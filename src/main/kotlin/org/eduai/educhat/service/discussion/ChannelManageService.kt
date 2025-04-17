package org.eduai.educhat.service.discussion

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import org.eduai.educhat.dto.message.MessageDto
import org.eduai.educhat.repository.DiscThreadHistRepository
import org.eduai.educhat.service.CustomRedisMessageListener
import org.eduai.educhat.service.KeyGeneratorService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.*

@Service
class ChannelManageService(
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val redisTemplate: StringRedisTemplate,
    private val customRedisMessageListener: CustomRedisMessageListener,
    private val keyGenService: KeyGeneratorService,
    private val discThreadHistRepository: DiscThreadHistRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChannelManageService::class.java)
    }

    @PostConstruct
    fun restoreChannelsFromRedis() {
        val sessionKeys = redisTemplate.keys("cls_sessions:*") // ✅ 모든 채널 키 조회
        sessionKeys.forEach { sessionKey ->
            val existingChannels = redisTemplate.opsForHash<String, String>().entries(sessionKey)
            existingChannels.forEach { (sessionHashKey, topicName) ->
                val grpId = keyGenService.decodeRedisSessionHashKey(sessionHashKey)
                val topic = ChannelTopic(topicName)

                // ✅ Redis Pub/Sub 채널 복원
                redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
                logger.info("🔄 Redis에서 기존 채팅방 복원: $topicName (Group ID: $grpId)")

                // ✅ 최신 100개 메시지를 Redis에 캐싱 (이전 메시지 빠른 조회 지원)
                cacheRecentMessages( keyGenService.decodeRedisSessionKey(sessionKey), grpId)
            }
        }
    }

    fun createGroupChannel(topicName: String) {
        val topic = ChannelTopic(topicName)
        redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
        logger.info("✅ 채팅방 생성 및 구독 등록: $topicName")
    }

    fun removeGroupChannel(topicName: String) {
        val topic = ChannelTopic(topicName)
        redisMessageListenerContainer.removeMessageListener(customRedisMessageListener, topic)
        logger.info("🛑 채팅방 구독 해제됨: $topicName")
    }

    private fun cacheRecentMessages(clsId: String, grpId: String) {
        val redisKey = keyGenService.generateChatLogsKey(clsId, grpId)
        redisTemplate.delete(redisKey)
        logger.info("🔄 기존 Redis 캐시 삭제: $redisKey")

        val recentMessages = discThreadHistRepository.findTop100ByClsIdAndGrpIdOrderByInsDtDesc(clsId, UUID.fromString(grpId))

        if (recentMessages.isNotEmpty()) {
            val objectMapper = jacksonObjectMapper()
            val seoulZoneId = ZoneId.of("Asia/Seoul") // 시간대 일관성 유지

            val messageJsonList = recentMessages.map { msg ->
                val msgDto = MessageDto(
                    clsId = clsId,
                    sender = msg.userId,
                    senderName = msg.userName,
                    grpId = grpId,
                    message = msg.msg,
                    timestamp = msg.insDt.atZone(seoulZoneId).toInstant().toString() // 서울 시간대 기준 -> UTC 문자열
                )
                objectMapper.writeValueAsString(msgDto) // JSON 문자열로 변환
            }

            redisTemplate.opsForList().leftPushAll(redisKey, messageJsonList)

            logger.info("📝 Redis에 최신 ${recentMessages.size}개 메시지 캐싱 완료 (기존 캐시 삭제 후): $grpId")
        } else {
            logger.info("ℹ️ DB에 캐싱할 최신 메시지가 없음: $clsId, $grpId")
        }
    }
}
