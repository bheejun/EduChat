package org.eduai.educhat.config

import org.eduai.educhat.service.RedisSubscribeService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import java.util.UUID


@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 6379

    @Value("\${spring.data.redis.password}")
    private lateinit var redisPassword: String

    @Bean
    fun redisConnectionFactory() : RedisConnectionFactory {
        return LettuceConnectionFactory(redisHost, redisPort)
    }

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(factory)
    }

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("chatroom") // Redis에서 사용할 채널명
    }

    @Bean
    fun redisMessageListener(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(listenerAdapter, topic())
        return container
    }

    @Bean
    fun messageListenerAdapter(subscriber: RedisSubscribeService): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber, "receiveMessage")
    }

    fun createChannelByGroup(groupId: UUID): ChannelTopic {

        return ChannelTopic(groupId.toString())
    }

}