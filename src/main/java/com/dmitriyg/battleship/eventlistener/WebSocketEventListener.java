package com.dmitriyg.battleship.eventlistener;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.MessagingData;
import com.dmitriyg.battleship.model.UserSession;
import com.dmitriyg.battleship.service.UserSessionService;

@Component
public class WebSocketEventListener {
	
	@Autowired
	private UserSessionService userSessionService;

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate; 
	
	private static final String DISCONNECT = "DISCONNECT";

	@EventListener
	private void handleSessionConnected(SessionSubscribeEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());

		userSessionService.add(headers);
		alertDestination(headers);
		updateUserList(headers);
	}

	@EventListener
	private void handleSessionDisconnected(SessionDisconnectEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());

		alertDestination(headers);
		updateUserList(headers);
		userSessionService.remove(headers); // remove userSession at the end else previous methods won't have access to it
	}

	private void updateUserList(SimpMessageHeaderAccessor headers) {
		String destination = headers.getDestination(); // null if messageType is disconnect
		
		if (headers.getMessageType().toString().equals(DISCONNECT)) {
			UserSession userSession = userSessionService.find(headers.getSessionId());
			destination = userSession.getDestination();
		}

		Set<String> users = userSessionService.findUsersSubscribedToTopic(destination);

		simpMessagingTemplate.convertAndSend(destination, 
				new MessagingData("user-status", HtmlUtils.htmlEscape("there are now (" + users.size() + ") users in room: " + destination)));
		
	}
	
	private void alertDestination(SimpMessageHeaderAccessor headers) {
		// onSubscribe
		String username = headers.getUser().getName();
		String destination = headers.getDestination(); // null if messageType is disconnect
		String message = " has JOINED the room!---";
		
		// onDisconnect
		if (headers.getMessageType().toString().equals(DISCONNECT)) {
			UserSession userSession = userSessionService.find(headers.getSessionId());
			username = userSession.getPrincipal().getName();
			destination = userSession.getDestination();
			message = " has LEFT the room!---";
		}

		simpMessagingTemplate.convertAndSend(destination, 
				new MessagingData("user-status", HtmlUtils.htmlEscape("---" + username + message)));
		
	}

}
