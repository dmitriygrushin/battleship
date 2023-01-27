package com.dmitriyg.battleship.config;

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
		// where the client(Browser) will subscribe to i.e. a room | @MessageMapping("/...")
		config.enableSimpleBroker("/topic"); 
		// prefix where client(Browser) will send their data i.e. stompClient.send("/prefix/...", ...) | @SendTo("/prefix/...")
		config.setApplicationDestinationPrefixes("/app"); 
		config.setUserDestinationPrefix("/user");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/fallback-websocket").withSockJS(); // SockJS fallback 
	}

}
