package org.eduai.educhat.service

import org.eduai.educhat.util.handler.WebSocketHandler
import org.springframework.stereotype.Service

@Service
class WebSocketService(
    private val webSocketHandler: WebSocketHandler
) {
    fun sendMessage(groupId: String, message: String) {
        println("WebSocket 전송: 그룹($groupId) → $message")
        webSocketHandler.sendMessageToGroup(groupId, message)
    }
}