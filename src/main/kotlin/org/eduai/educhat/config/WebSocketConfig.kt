package org.eduai.educhat.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig  : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/disc/ws")
            .setAllowedOriginPatterns(
                "http://localhost:3000",
                "http://58.29.36.4:3500",
                "https://tutor.k-university.ai",
                "http://27.96.151.215:3500")
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/disc/subs")
        registry.setApplicationDestinationPrefixes("/disc/thread")
    }
}
