package com.dmitriyg.battleship.eventlistener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dmitriyg.battleship.service.UserSessionService;

@Component
public class WebSocketEventListener {
	
	@Autowired
	private UserSessionService userSessionService;

	@EventListener
	private void handleSessionConnected(SessionSubscribeEvent event) {
		userSessionService.add(event);
		// send connect alert to destination
	}

	@EventListener
	private void handleSessionDisconnected(SessionDisconnectEvent event) {
		userSessionService.remove(event);
		// send disconnect alert to destination
	}

}
