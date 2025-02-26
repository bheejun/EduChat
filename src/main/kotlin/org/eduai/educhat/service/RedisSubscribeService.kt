package org.eduai.educhat.service

import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class RedisSubscribeService(
    private val messagingTemplate: SimpMessagingTemplate  // WebSocket 메시지 전송
) : MessageListener {

    companion object {
        private val logger = LoggerFactory.getLogger(RedisSubscribeService::class.java)
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val msg = String(message.body, StandardCharsets.UTF_8)
        val channel = String(message.channel, StandardCharsets.UTF_8)

        logger.info("📩 Redis 메시지 수신: $msg (채널: $channel)")

        messagingTemplate.convertAndSend("/disc/thread/$channel", msg)
    }
}
