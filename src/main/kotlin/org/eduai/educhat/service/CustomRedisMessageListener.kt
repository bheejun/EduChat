package org.eduai.educhat.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eduai.educhat.dto.ReceivedMessageDto
import org.eduai.educhat.service.impl.ThreadManageServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class CustomRedisMessageListener(
    private val messagingTemplate: SimpMessagingTemplate // ✅ WebSocket 메시지 전송
) : MessageListener {

    companion object {
        private val logger = LoggerFactory.getLogger(CustomRedisMessageListener::class.java)
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val msg = String(message.body, StandardCharsets.UTF_8)
            val channel = String(message.channel, StandardCharsets.UTF_8)

            logger.info("📩 Redis 메시지 수신: $msg (채널: $channel)")

            val messageObj = jacksonObjectMapper().readValue(msg, ReceivedMessageDto::class.java)

            // ✅ WebSocket을 통해 클라이언트에게 메시지 전송
            messagingTemplate.convertAndSend("/discussion/subs/${messageObj.grpId}", messageObj)

        } catch (e: Exception) {
            logger.info("🚨 메시지 변환 실패: ${e.message}")
        }
    }
}
