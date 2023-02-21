package com.dmitriyg.battleship.eventlistener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dmitriyg.battleship.service.UserSessionService;
import com.dmitriyg.battleship.util.WebSocketUtils;

@Component
public class WebSocketEventListener {
	
	@Autowired
	private UserSessionService userSessionService;
	
	@Autowired
	private WebSocketUtils webSocketUtils;

	@EventListener
	private void handleSessionConnected(SessionSubscribeEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		
		// stop user destination: /user from being used because they are redundant since /topic is already being used when users join a room.
		if (headers.getDestination().indexOf("/user") == 0) return;

		userSessionService.add(headers);
		webSocketUtils.alertDestination(headers);
		webSocketUtils.updateUserList(headers);
		webSocketUtils.sendUsernames(headers);
	}

	@EventListener
	private void handleSessionDisconnected(SessionDisconnectEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		
		webSocketUtils.alertDestination(headers);
		webSocketUtils.updateUserList(headers);
		userSessionService.remove(headers); // remove userSession at the end else previous methods won't have access to it
	}

	
	

}
