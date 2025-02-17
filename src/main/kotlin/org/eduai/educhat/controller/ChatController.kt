package org.eduai.educhat.controller

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller


@Controller
class ChatController(
    private val redisTemplate: StringRedisTemplate
) {

    @MessageMapping("/discussion/thread/{groupId}")
    fun sendMessage(@Payload message: String, @DestinationVariable groupId: String) {
        val topicName = "chat:$groupId"
        redisTemplate.convertAndSend(topicName, message)
        println("📤 메시지 발행됨: $message → Redis 채널: $topicName")
    }
}