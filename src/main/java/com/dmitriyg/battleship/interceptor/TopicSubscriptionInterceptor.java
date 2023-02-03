package com.dmitriyg.battleship.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.dmitriyg.battleship.service.UserSessionService;

@Component
public class TopicSubscriptionInterceptor implements ChannelInterceptor {
	
	@Autowired 
	private UserSessionService userSessionService;
	
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
    	StompHeaderAccessor header = StompHeaderAccessor.wrap(message);
    	String destination = header.getDestination();

    	if (header.getMessageType() == SimpMessageType.SUBSCRIBE && 
    			userSessionService.findUsersSubscribedToTopic(destination).size() >= 2) {
    		throw new RuntimeException("Topic subscription limit reached");
    	}

    	return message;
    }

}
