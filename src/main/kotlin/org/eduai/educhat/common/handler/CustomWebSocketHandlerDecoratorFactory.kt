package org.eduai.educhat.common.handler

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.WebSocketHandlerDecorator
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory
import java.util.concurrent.ConcurrentHashMap

@Component
class CustomWebSocketHandlerDecoratorFactory : WebSocketHandlerDecoratorFactory {

    // 모든 WebSocketSession을 세션 ID를 Key로 저장
    private val sessionMap: MutableMap<String, WebSocketSession> = ConcurrentHashMap()

    override fun decorate(handler: WebSocketHandler): WebSocketHandler {
        return object : WebSocketHandlerDecorator(handler) {
            override fun afterConnectionEstablished(session: WebSocketSession) {
                sessionMap[session.id] = session
                super.afterConnectionEstablished(session)
            }

            override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
                sessionMap.remove(session.id)
                super.afterConnectionClosed(session, closeStatus)
            }
        }
    }

    fun getSession(sessionId: String): WebSocketSession? = sessionMap[sessionId]

    fun disconnectSession(sessionId: String) {
        val session = sessionMap[sessionId]
        if (session != null && session.isOpen) {
            session.close(CloseStatus.NORMAL)
        }
    }
}
