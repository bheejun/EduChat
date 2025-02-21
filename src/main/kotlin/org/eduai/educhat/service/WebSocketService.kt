package org.eduai.educhat.service

import org.eduai.educhat.service.impl.ThreadManageServiceImpl
import org.eduai.educhat.util.handler.WebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WebSocketService(
    private val webSocketHandler: WebSocketHandler
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WebSocketService::class.java)
    }
    fun sendMessage(groupId: String, message: String) {
        logger.info("WebSocket 전송: 그룹($groupId) → $message")
        webSocketHandler.sendMessageToGroup(groupId, message)
    }
}