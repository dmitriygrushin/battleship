package com.dmitriyg.battleship.service;

import java.util.Map;
import java.util.Set;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import com.dmitriyg.battleship.model.UserSession;

public interface UserSessionService {

	void add(SimpMessageHeaderAccessor headers);
	void remove(SimpMessageHeaderAccessor headers);
	UserSession find(String sessionId);
	Map<String, UserSession> getActiveUserSessions();
	void setActiveUserSessions(Map<String, UserSession> activeUserSessions);
	Set<String> findUsersSubscribedToTopic(String topic);
}
