package org.eduai.educhat.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class RedisSubscribeService(
    private val messagingTemplate: SimpMessagingTemplate  // WebSocket 메시지 전송
) : MessageListener {

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val msg = String(message.body, StandardCharsets.UTF_8)
        val channel = String(message.channel, StandardCharsets.UTF_8)

        println("📩 Redis 메시지 수신: $msg (채널: $channel)")

        messagingTemplate.convertAndSend("/discussion/thread/$channel", msg)
    }
}
