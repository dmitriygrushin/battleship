package com.dmitriyg.battleship.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.dmitriyg.battleship.model.UserSession;

/*
 * Store Non-permanent data about active sessions i.e. users connected/subscribed to topics 
 */

@Repository
public class UserSessionRepository {

	private Map<String, UserSession> activeUserSessions = new ConcurrentHashMap<>();
	
	public void add(String sessionId, UserSession userSession) {
		activeUserSessions.put(sessionId, userSession);
	}
	
	public UserSession getUserSession(String sessionId) {
		return activeUserSessions.get(sessionId);
	}

	public void remove(String sessionId) {
		activeUserSessions.remove(sessionId);
	}

	public Map<String, UserSession> getActiveUserSessions() {
		return activeUserSessions;
	}

	public void setActiveUserSessions(Map<String, UserSession> activeUserSessions) {
		this.activeUserSessions = activeUserSessions;
	}
	
}
