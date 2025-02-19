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
    private val customRedisMessageListener: CustomRedisMessageListener,
    private val keyGenService: KeyGeneratorService
) {

    @PostConstruct
    fun restoreChannelsFromRedis() {
        //프로퍼티스로 관리하면서 암호화 고려
        val clsSessionKey = redisTemplate.keys(keyGenService.generateRestoreKey()) // ✅ 저장된 세션 목록 가져오기
        clsSessionKey.forEach { sessionKey ->
            val existingChannels = redisTemplate.opsForHash<String, String>().entries(sessionKey)
            existingChannels.forEach { (groupId, topicName) ->
                val topic = ChannelTopic(topicName)

                redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
                println("🔄 Redis에서 기존 채팅방 복원: $topicName (Group ID: $groupId)")
            }
        }
    }

    fun createGroupChannel(sessionGrpKey:String) {
        val topic = ChannelTopic(sessionGrpKey)

        redisMessageListenerContainer.addMessageListener(customRedisMessageListener, topic)
        println("✅ 채팅방 생성 및 구독 등록: $sessionGrpKey")
    }

    fun removeGroupChannel(sessionGrpKey: String) {
        val topic = ChannelTopic(sessionGrpKey)

        redisMessageListenerContainer.removeMessageListener(customRedisMessageListener, topic)
        println("🛑 채팅방 구독 해제됨: $sessionGrpKey")
    }
}
