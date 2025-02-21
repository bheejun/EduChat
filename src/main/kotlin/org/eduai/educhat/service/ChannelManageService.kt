package org.eduai.educhat.service

import jakarta.annotation.PostConstruct
import org.eduai.educhat.service.impl.ThreadManageServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChannelManageService(
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val redisTemplate: StringRedisTemplate,
    private val customRedisMessageListener: CustomRedisMessageListener,
    private val keyGenService: KeyGeneratorService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ChannelManageService::class.java)
    }

    @PostConstruct
    fun restoreChannelsFromRedis() {
        //프로퍼티스로 관리하면서 암호화 고려
        val clsSessionKey = redisTemplate.keys(keyGenService.generateRestoreKey()) // ✅ 저장된 세션 목록 가져오기
        clsSessionKey.forEach { sessionKey ->
            val existingChannels = redisTemplate.opsForHash<String, String>().entries(sessionKey)
            existingChannels.forEach { (groupId, topicName) ->
                val topic = ChannelTopic(topicName)

                redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
                logger.info("🔄 Redis에서 기존 채팅방 복원: $topicName (Group ID: $groupId)")
            }
        }
    }

    // 이제 topicName을 직접 사용해서 구독을 등록
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
}
