package org.eduai.educhat.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service


@Service
class RedisPublishService(
    private val redisTemplate: StringRedisTemplate,
    private val topic: ChannelTopic
) {

    fun publishMessage(message: String) {
        val encodedMessage = message.toByteArray(Charsets.UTF_8)
        redisTemplate.convertAndSend(topic.topic, message)
    }


}