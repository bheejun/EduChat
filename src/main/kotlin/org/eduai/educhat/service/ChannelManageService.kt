package org.eduai.educhat.service

import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChannelManageService(
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val redisTemplate: StringRedisTemplate,
    private val customRedisMessageListener: CustomRedisMessageListener
) {

    @PostConstruct
    fun restoreChannelsFromRedis() {
        //프로퍼티스로 관리하면서 암호화 고려
        val sessionKeys = redisTemplate.keys("chat_sessions:*") // ✅ 저장된 세션 목록 가져오기
        sessionKeys.forEach { sessionKey ->
            val existingChannels = redisTemplate.opsForHash<String, String>().entries(sessionKey)
            existingChannels.forEach { (groupId, topicName) ->
                val topic = ChannelTopic(topicName)

                redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
                println("🔄 Redis에서 기존 채팅방 복원: $topicName (Group ID: $groupId)")
            }
        }
    }

    fun createGroupChannel(groupId: UUID) {
        val topicName = "chat:$groupId"
        val topic = ChannelTopic(topicName)

        redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
        println("✅ 채팅방 생성 및 구독 등록: $topicName")
    }

    fun removeGroupChannel(groupId: UUID) {
        val topicName = "chat:$groupId"
        val topic = ChannelTopic(topicName)

        redisMessageListenerContainer.removeMessageListener(customRedisMessageListener, topic)
        println("🛑 채팅방 구독 해제됨: $topicName")
    }
}
