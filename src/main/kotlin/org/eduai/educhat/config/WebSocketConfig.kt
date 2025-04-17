package org.eduai.educhat.config

import org.eduai.educhat.common.handler.CustomWebSocketHandlerDecoratorFactory
import org.eduai.educhat.service.discussion.ThreadChannelInterceptor
import org.eduai.educhat.service.discussion.ThreadSessionManager
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig (
    private val customWebSocketHandlerDecoratorFactory: CustomWebSocketHandlerDecoratorFactory,
    private val threadChannelInterceptor: ThreadChannelInterceptor
)  : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/disc/ws")
            .setAllowedOriginPatterns(
                "http://localhost:3000",
                "http://58.29.36.4:3500",
                "http://58.29.36.4",
                "https://tutor.k-university.ai",
                "http://27.96.151.215:3500")
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/disc/subs")
        registry.setApplicationDestinationPrefixes("/disc/thread")
    }

    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        registry.addDecoratorFactory(customWebSocketHandlerDecoratorFactory)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(threadChannelInterceptor)
    }
}
