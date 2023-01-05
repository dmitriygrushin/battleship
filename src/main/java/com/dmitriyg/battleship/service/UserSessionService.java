package com.dmitriyg.battleship.service;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import com.dmitriyg.battleship.model.UserSession;

public interface UserSessionService {

	void add(SimpMessageHeaderAccessor headers);
	UserSession getUserSession(String sessionId);
	void remove(SimpMessageHeaderAccessor headers);
	Map<String, UserSession> getActiveUserSessions();
	void setActiveUserSessions(Map<String, UserSession> activeUserSessions);
	void alertDestinationOnSubscribe(SimpMessageHeaderAccessor headers);
	void alertDestinationOnDisconnect(SimpMessageHeaderAccessor headers);
	void updateUserListOnSubscribe(SimpMessageHeaderAccessor headers);
	void updateUserListOnDisconnect(SimpMessageHeaderAccessor headers);
}
