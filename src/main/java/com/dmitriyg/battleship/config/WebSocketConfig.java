package com.dmitriyg.battleship.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.dmitriyg.battleship.interceptor.TopicSubscriptionInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	private TopicSubscriptionInterceptor topicSubscriptionInterceptor;
	
	@Autowired
	public WebSocketConfig(@Lazy TopicSubscriptionInterceptor topicSubscriptionInterceptor) {
		this.topicSubscriptionInterceptor = topicSubscriptionInterceptor;
	}

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
		// NOTE TO SELF!: .setAllowedOriginsPatterns("*") allows Web Socket to work on server using Nginx, but imposes CSRF attacks so that would need to be handled if the application wasn't just a silly game. 
		registry.addEndpoint("/fallback-websocket").setAllowedOriginPatterns("*").withSockJS(); 
	}
	
	/* clientInboundChannel: For passing messages received from WebSocket clients.
	 * clientOutboundChannel: For sending server messages to WebSocket clients. */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(topicSubscriptionInterceptor);
	}

}
