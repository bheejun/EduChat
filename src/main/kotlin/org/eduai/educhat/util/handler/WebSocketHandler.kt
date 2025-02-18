package org.eduai.educhat.util.handler

import org.eduai.educhat.dto.request.SendMessageRequestDto
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class WebSocketHandler : TextWebSocketHandler() {

    private val sessions = mutableMapOf<String, MutableList<WebSocketSession>>() // <groupId, List<WebSocketSession>>

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val groupId = session.attributes["groupId"] as? String ?: return
        sessions.computeIfAbsent(groupId) { mutableListOf() }.add(session)
        println("WebSocket 연결됨: 그룹($groupId)")
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        println(message.payload.toString())
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("웹소켓 메시지 수신: ${message.payload}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        val groupId = session.attributes["groupId"] as? String ?: return
        sessions[groupId]?.remove(session)
        println("WebSocket 연결 종료: 그룹($groupId)")
    }

    fun sendMessageToGroup(groupId: String, message: String) {
        val targetSessions = sessions[groupId] ?: return
        for (session in targetSessions) {
            if (session.isOpen) {
                session.sendMessage(TextMessage(message))
            }
        }
        println("WebSocket 메시지 전송 완료: 그룹($groupId) → $message")
    }


}