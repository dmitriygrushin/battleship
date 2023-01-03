package com.dmitriyg.battleship.service;

import java.util.Map;

import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dmitriyg.battleship.model.UserSession;

public interface UserSessionService {

	void add(SessionSubscribeEvent event);

	UserSession getUserSession(String sessionId);

	void remove(SessionDisconnectEvent event);

	Map<String, UserSession> getActiveUserSessions();

	void setActiveUserSessions(Map<String, UserSession> activeUserSessions);
}
