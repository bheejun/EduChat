package org.eduai.educhat.common.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class WebSocketHandler : TextWebSocketHandler() {

    companion object {
        private val logger = LoggerFactory.getLogger(WebSocketHandler::class.java)
    }

    private val sessions = mutableMapOf<String, MutableList<WebSocketSession>>() // <groupId, List<WebSocketSession>>

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val groupId = session.attributes["groupId"] as? String ?: return
        sessions.computeIfAbsent(groupId) { mutableListOf() }.add(session)
        logger.info("WebSocket 연결됨: 그룹($groupId)")
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        logger.info(message.payload.toString())
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.info("웹소켓 메시지 수신: ${message.payload}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        val groupId = session.attributes["groupId"] as? String ?: return
        sessions[groupId]?.remove(session)
        logger.info("WebSocket 연결 종료: 그룹($groupId)")
    }


}