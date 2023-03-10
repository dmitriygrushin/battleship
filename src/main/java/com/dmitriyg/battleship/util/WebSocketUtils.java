package com.dmitriyg.battleship.util;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.MessagingData;
import com.dmitriyg.battleship.model.UserSession;
import com.dmitriyg.battleship.service.UserSessionService;

@Component
public class WebSocketUtils {
	
	@Autowired
	private UserSessionService userSessionService;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate; 
	@Autowired
	private SimpUserRegistry userRegistry;
	
	public void processUserSession(SimpMessageHeaderAccessor headers) {
		String username;
		String destination;
		String userStatusType;

		if (headers.getMessageType() == SimpMessageType.SUBSCRIBE) {
			username = headers.getUser().getName();
			destination = headers.getDestination();
			userStatusType = "user-status-connect";
			broadcastToTopic(username, destination, new MessagingData<>("user-status-alert", HtmlUtils.htmlEscape("---" + username + " has JOINED the room!---")));
		} else if (headers.getMessageType() == SimpMessageType.DISCONNECT) {
			UserSession userSession = userSessionService.find(headers.getSessionId());
			if (userSession == null) return;
			username = userSession.getPrincipal().getName();
			destination = userSession.getDestination();
			userStatusType = "user-status-disconnect";
			broadcastToTopic(username, destination, new MessagingData<>("user-status-alert", HtmlUtils.htmlEscape("---" + username + " has LEFT the room!---")));
		} else {
			return;
		}

		simpMessagingTemplate.convertAndSend(destination, new MessagingData<String>(userStatusType, ""));
	}

	
	
	// send usernames from client to remove their name which leaves them with opponent's username
	public void sendUsernames(SimpMessageHeaderAccessor headers) {
		if (headers.getMessageType() == SimpMessageType.SUBSCRIBE) {
			String destination = headers.getDestination();
			Set<String> users = userSessionService.findUsersSubscribedToTopic(destination);
			simpMessagingTemplate.convertAndSend(destination, 
					new MessagingData<Set<String>>("usernames", users));
		}
	}
	
	
	// send message to all subscribers of a topic except the sender
	public void broadcastToTopic(String broadcasterUsername, String destination, MessagingData<String> message) {
		Set<String> usernames =  userSessionService.findUsersSubscribedToTopic(destination);

		// remove the user that's broadcasting the message
		usernames.remove(broadcasterUsername);
		
		for (String username : usernames) {
			simpMessagingTemplate.convertAndSendToUser(username, destination, message);
		}
		
	}
	
	// check if both users subscribed to the topic are ready
	public boolean isTopicReady(String destination) {
		Set<SimpSession> sessions = userSessionService.findSessionsSubscribedToTopic(destination);
		
		if (sessions.size() < 2) return false;
		
		for (SimpSession session : sessions) {
			if (!userSessionService.find(session.getId()).isReady()) return false;
		}

		return true;
	}
	
	public void printUsersSubscribedToTopic(String topic) {
		userRegistry.findSubscriptions(subscription -> 
			subscription.getDestination().equals(topic))
		.forEach(subscription -> 
			System.out.println(subscription.getSession().getId())
		);
	}

}
