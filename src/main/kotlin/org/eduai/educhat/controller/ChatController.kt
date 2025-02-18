package org.eduai.educhat.controller

import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.eduai.educhat.service.ThreadManageService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody


@Controller
class ChatController(
    private val threadManageService: ThreadManageService
) {

    @MessageMapping("/pub/{groupId}")
    fun sendMessage(@RequestBody messageDto: SendMessageRequestDto, @DestinationVariable groupId: String) {
        val message = "${messageDto.sender}: ${messageDto.message}"
        val topicName = "chat:$groupId"
        threadManageService.sendMessageToRedis(messageDto)
        println("📤 메시지 발행됨: $message → Redis 채널: $topicName")
    }
}