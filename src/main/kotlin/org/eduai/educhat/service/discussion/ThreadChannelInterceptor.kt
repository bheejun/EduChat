package org.eduai.educhat.service.discussion

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class ThreadChannelInterceptor(
    private val threadSessionManager: ThreadSessionManager
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
        accessor?.let {
            when (it.command) {
                StompCommand.SUBSCRIBE -> {
                    val destination = it.destination
                    if (destination != null && destination.startsWith("/disc/subs/")) {
                        val threadId = destination.removePrefix("/disc/subs/")
                        val sessionId = it.sessionId
                        sessionId?.let { sid ->
                            threadSessionManager.addSessionToThread(threadId, sid)
                        }
                    } else {
                        throw IllegalArgumentException("Not Valid Session")
                    }
                }
                StompCommand.DISCONNECT -> {
                    it.sessionId?.let { sid ->
                        threadSessionManager.removeSession(sid)
                    }
                }
                else -> { /* 그 외 명령은 무시 */ }
            }
        }
        return message
    }
}
