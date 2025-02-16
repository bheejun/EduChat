package org.eduai.educhat.service

import org.springframework.stereotype.Service

@Service
class WebSocketService {
    fun sendMessageToClients(groupId: String, message: String) {
        println("WebSocket 전송: 그룹($groupId) → $message")
    }
}