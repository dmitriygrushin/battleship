package com.dmitriyg.battleship.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import com.dmitriyg.battleship.model.UserSession;
import com.dmitriyg.battleship.repository.UserSessionRepository;

@Service
public class UserSessionServiceImpl implements UserSessionService {

	@Autowired
	private UserSessionRepository userSessionRepository;

	@Autowired
	private SimpUserRegistry userRegistry;


	@Override
	public UserSession find(String sessionId) {
		return userSessionRepository.find(sessionId);
	}
	
	@Override
	public void add(SimpMessageHeaderAccessor headers) {
		userSessionRepository.add(headers.getSessionId(), new UserSession(headers.getUser(), headers.getDestination()));
	}

	@Override
	public void remove(SimpMessageHeaderAccessor headers) {
		userSessionRepository.remove(headers.getSessionId());
	}
	
	@Override
	public Map<String, UserSession> getActiveUserSessions() {
		return userSessionRepository.getActiveUserSessions();
	}

	@Override
	public void setActiveUserSessions(Map<String, UserSession> activeUserSessions) {
		userSessionRepository.setActiveUserSessions(activeUserSessions);
	}
	
	@Override
	public Set<String> findUsersSubscribedToTopic(String topic) {
		Set<String> users = new HashSet<>();
		
		userRegistry.findSubscriptions(subscription -> 
			subscription.getDestination().equals(topic))
		.forEach(subscription -> 
			users.add(subscription.getSession().getUser().getName())
		);
		
		return users;
	}

	@Override
	public Set<SimpSession> findSessionsSubscribedToTopic(String topic) {
		Set<SimpSession> session = new HashSet<>();
		
		userRegistry.findSubscriptions(subscription -> 
			subscription.getDestination().equals(topic))
		.forEach(subscription -> 
			session.add(subscription.getSession())
		);
		
		return session;
	}
}
