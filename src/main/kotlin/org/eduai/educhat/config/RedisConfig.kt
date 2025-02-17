package org.eduai.educhat.config

import org.eduai.educhat.service.RedisSubscribeService
import org.eduai.educhat.service.impl.ThreadManageServiceImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import java.util.*

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 6379

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(redisHost, redisPort)
    }

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(factory)
    }

    @Bean
    fun redisMessageListener(
        connectionFactory: RedisConnectionFactory
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
        }
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        subsService: RedisSubscribeService,
        redisTemplate: StringRedisTemplate
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)

        val existingGroups = redisTemplate.keys("chat:*")
        existingGroups.forEach { topicName ->
            container.addMessageListener(subsService, ChannelTopic(topicName))
            println("Redis 채널 구독: $topicName")
        }

        return container
    }


    @Bean
    fun messageListenerAdapter(threadManageService: ThreadManageServiceImpl): MessageListenerAdapter {
        return MessageListenerAdapter(threadManageService, "receiveMessage")
    }

//    @Bean
    fun restoreChannelsFromRedis(
        redisMessageListenerContainer: RedisMessageListenerContainer,
        threadManageService: ThreadManageServiceImpl,
        redisTemplate: StringRedisTemplate
    ) {
        val keys = redisTemplate.keys("chat_sessions:*")
        keys.forEach { key ->
            val existingSessions = redisTemplate.opsForHash<String, String>().entries(key)
            existingSessions.forEach { (groupId, topicName) ->
                val uuid = UUID.fromString(groupId)
                val topic = ChannelTopic(topicName)
                val listenerAdapter = MessageListenerAdapter(threadManageService, "receiveMessage")
                redisMessageListenerContainer.addMessageListener(listenerAdapter, topic)
                println("🔄 Redis에서 기존 채팅방 복원: $topicName (Group ID: $groupId)")
            }
        }
    }

    fun addChannelForGroup(
        redisMessageListenerContainer: RedisMessageListenerContainer,
        threadManageService: ThreadManageServiceImpl,
        groupId: UUID
    ) {
        val topicName = "chat:$groupId"
        val topic = ChannelTopic(topicName)
        val listenerAdapter = MessageListenerAdapter(threadManageService, "receiveMessage")
        redisMessageListenerContainer.addMessageListener(listenerAdapter, topic)
        println("✅ 채널 등록됨: $topicName (Group ID: $groupId)")
    }

    fun removeChannelForGroup(
        redisMessageListenerContainer: RedisMessageListenerContainer,
        groupId: UUID
    ) {
        val topicName = "chat:$groupId"
        val topic = ChannelTopic(topicName)
        redisMessageListenerContainer.removeMessageListener(null, topic)
        println("🛑 채널 해제됨: $topicName (Group ID: $groupId)")
    }

    fun subscribeNewGroupChannel(container: RedisMessageListenerContainer, subsService: RedisSubscribeService, groupId: String) {
        val topic = ChannelTopic("chat:$groupId")
        container.addMessageListener(subsService, topic)
        println("✅ 새 채팅방 구독됨: chat:$groupId")
    }

}
