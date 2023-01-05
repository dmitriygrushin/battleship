package com.dmitriyg.battleship.eventlistener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dmitriyg.battleship.model.UserSession;
import com.dmitriyg.battleship.service.UserSessionService;

@Component
public class WebSocketEventListener {
	
	@Autowired
	private UserSessionService userSessionService;

	@EventListener
	private void handleSessionConnected(SessionSubscribeEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());

		userSessionService.add(headers);
		userSessionService.alertDestinationOnSubscribe(headers);
		userSessionService.updateUserListOnSubscribe(headers);
	}

	@EventListener
	private void handleSessionDisconnected(SessionDisconnectEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());

		userSessionService.alertDestinationOnDisconnect(headers);
		userSessionService.updateUserListOnDisconnect(headers);
		userSessionService.remove(headers); // remove userSession at the end else previous methods won't have access to it
	}
	

}
