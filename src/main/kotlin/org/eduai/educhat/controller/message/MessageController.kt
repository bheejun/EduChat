package org.eduai.educhat.controller.message

import org.eduai.educhat.dto.message.request.SendMessageRequestDto
import org.eduai.educhat.service.discussion.ThreadManageService
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody


@Controller
class MessageController(
    private val threadManageService: ThreadManageService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MessageController::class.java)
    }

    @MessageMapping("/pub/{groupId}")
    fun sendMessage(@RequestBody messageDto: SendMessageRequestDto, @DestinationVariable groupId: String) {
        val message = "${messageDto.sender}: ${messageDto.message}"
        val topicName = "chat:$groupId"
        threadManageService.sendMessageToRedis(messageDto)
        logger.info("📤 메시지 발행됨: $message → Redis 채널: $topicName")
    }
}