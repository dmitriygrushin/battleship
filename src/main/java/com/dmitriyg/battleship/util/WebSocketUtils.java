package com.dmitriyg.battleship.util;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.MessagingData;
import com.dmitriyg.battleship.model.UserSession;
import com.dmitriyg.battleship.service.UserService;
import com.dmitriyg.battleship.service.UserSessionService;

@Component
public class WebSocketUtils {
	
	@Autowired
	private UserSessionService userSessionService;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate; 
	
	public void updateUserList(SimpMessageHeaderAccessor headers) {
		String destination; // null if messageType is disconnect
		String messageType;

		if (headers.getMessageType() == SimpMessageType.SUBSCRIBE) {
			destination = headers.getDestination(); 
			messageType = "user-status-connect";
		} else if (headers.getMessageType() == SimpMessageType.DISCONNECT) {
			UserSession userSession = userSessionService.find(headers.getSessionId());
			if (userSession == null) return;
			destination = userSession.getDestination();
			messageType = "user-status-disconnect";
		} else {
			return;
		}

		Set<String> users = userSessionService.findUsersSubscribedToTopic(destination);

		simpMessagingTemplate.convertAndSend(destination, 
				new MessagingData<String>(messageType, HtmlUtils.htmlEscape("there are now (" + users.size() + ") users in room: " + destination)));
		
	}
	
	public void sendOpponentUsername(SimpMessageHeaderAccessor headers) {
		if (headers.getMessageType() == SimpMessageType.SUBSCRIBE) {
			String destination = headers.getDestination();
			Set<String> users = userSessionService.findUsersSubscribedToTopic(destination);
			simpMessagingTemplate.convertAndSend(destination, 
					new MessagingData<Set<String>>("usernames", users));
		}
	}
	
	public void alertDestination(SimpMessageHeaderAccessor headers) {
		String username;
		String destination; // null if messageType is disconnect
		String message;
		
		/* TODO: Fix Ugly if else return. You don't need perfection you need to move forward.
		 * Works for now to not get caught up in the perfectionism
		 * Alerts need to only work on subscribe and disconnect
		 */
		if (headers.getMessageType() == SimpMessageType.SUBSCRIBE) {
			System.out.println("subscribe if fired");
			username = headers.getUser().getName();
			destination = headers.getDestination(); // null if messageType is disconnect
			message = " has JOINED the room!---";
		} else if (headers.getMessageType() == SimpMessageType.DISCONNECT) {
			System.out.println("disconnect else if fired");
			UserSession userSession = userSessionService.find(headers.getSessionId());
			if (userSession == null) return;
			username = userSession.getPrincipal().getName();
			destination = userSession.getDestination();
			message = " has LEFT the room!---";
		} else {
			return;
		}

		broadcastToTopic(username, destination, 
				new MessagingData<String>("user-status-alert", HtmlUtils.htmlEscape("---" + username + message)));

	}
	
	// send message to all subscribers of a topic except the sender
	public void broadcastToTopic(String broadcaster, String destination, MessagingData message) {
		Set<String> usernames =  userSessionService.findUsersSubscribedToTopic(destination);
		System.out.println("broachcastToTopic.destination: " + destination + ": " + message.getContent());

		// remove the user that's broadcasting the message
		usernames.remove(broadcaster);
		
		for (String username : usernames) {
			simpMessagingTemplate.convertAndSendToUser(username, destination, message);
		}
		
	}


}
