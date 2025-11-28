package com.anish.fraud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // The frontend will subscribe to topics starting with "/topic"
        config.enableSimpleBroker("/topic");
        // The frontend will send messages to urls starting with "/app" (we might not use this today)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the "Door" the frontend connects to.
        // setAllowedOriginPatterns("*") fixes CORS issues so React can connect from port 3000
        registry.addEndpoint("/ws-fraud")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}