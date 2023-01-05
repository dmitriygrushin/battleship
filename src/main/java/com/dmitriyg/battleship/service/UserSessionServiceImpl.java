package com.dmitriyg.battleship.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.SubscriptionRegistry;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.MessagingData;
import com.dmitriyg.battleship.model.UserSession;
import com.dmitriyg.battleship.repository.UserSessionRepository;

@Service
public class UserSessionServiceImpl implements UserSessionService {

	@Autowired
	private UserSessionRepository userSessionRepository;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate; 
	@Autowired
	private SimpUserRegistry userRegistry;

	@Override
	public UserSession getUserSession(String sessionId) {
		return userSessionRepository.getUserSession(sessionId);
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
	public void add(SimpMessageHeaderAccessor headers) {
		userSessionRepository.add(headers.getSessionId(), new UserSession(headers.getUser(), headers.getDestination()));
	}

	@Override
	public void remove(SimpMessageHeaderAccessor headers) {
		UserSession userSession = userSessionRepository.getUserSession(headers.getSessionId());
		userSessionRepository.remove(headers.getSessionId());
	}

	@Override
	public void alertDestinationOnSubscribe(SimpMessageHeaderAccessor headers) {
		simpMessagingTemplate.convertAndSend(headers.getDestination(), 
				new MessagingData("user-status", HtmlUtils.htmlEscape(
						"---" + headers.getUser().getName() + " has JOINED the room!---")));
	}

	@Override
	public void alertDestinationOnDisconnect(SimpMessageHeaderAccessor headers) {
		// A userSession is RETRIEVED since on DISCONNECT the DESTINATION is lost from the HEADERS. 
		UserSession userSession = userSessionRepository.getUserSession(headers.getSessionId());

		simpMessagingTemplate.convertAndSend(userSession.getDestination(), 
				new MessagingData("user-status", HtmlUtils.htmlEscape(
						"---" + userSession.getPrincipal().getName() + " has LEFT the room!---")));
	}
	
	@Override
	public void updateUserListOnSubscribe(SimpMessageHeaderAccessor headers) {
		Set<String> users = findUsersSubscribedToTopic(headers.getDestination());
		
		simpMessagingTemplate.convertAndSend(headers.getDestination(), 
				new MessagingData("user-status", HtmlUtils.htmlEscape("there are now (" + users.size() + ") users in room: " + headers.getDestination())));
	}

	@Override
	public void updateUserListOnDisconnect(SimpMessageHeaderAccessor headers) { 
		// A userSession is RETRIEVED since on DISCONNECT the DESTINATION is lost from the HEADERS. 
		UserSession userSession = userSessionRepository.getUserSession(headers.getSessionId());

		Set<String> users = findUsersSubscribedToTopic(userSession.getDestination());
		
		simpMessagingTemplate.convertAndSend(userSession.getDestination(), 
				new MessagingData("user-status", HtmlUtils.htmlEscape("there are now (" + users.size() + ") users in room: " + userSession.getDestination())));
	}
	
	private Set<String> findUsersSubscribedToTopic(String topic) {
		Set<String> users = new HashSet<>();
		
		userRegistry.findSubscriptions(subscription -> 
			subscription.getDestination().equals(topic))
		.forEach(subscription -> 
			users.add(subscription.getSession().getUser().getName())
		);
		
		return users;
	}
}
