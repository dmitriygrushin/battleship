package com.dmitriyg.battleship.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.ChatMessage;
import com.dmitriyg.battleship.model.UserSession;
import com.dmitriyg.battleship.repository.UserSessionRepository;

@Service
public class UserSessionServiceImpl implements UserSessionService {

	@Autowired
	private UserSessionRepository userSessionRepository;
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate; 

	// add user to userSessionRepository and alert the user's destination of the user's connection
	@Override
	public void add(SessionSubscribeEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		userSessionRepository.add(headers.getSessionId(), new UserSession(headers.getUser(), headers.getDestination()));
		alertDestinationOnSubscribe(headers);
	}

	@Override
	public UserSession getUserSession(String sessionId) {
		return userSessionRepository.getUserSession(sessionId);
	}

	// remove user from userSessionRepository and alert the user's destination of the user's disconnection
	@Override
	public void remove(SessionDisconnectEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		UserSession userSession = userSessionRepository.getUserSession(headers.getSessionId());
		userSessionRepository.remove(headers.getSessionId());
		alertDestinationOnDisconnect(userSession);
	}

	@Override
	public Map<String, UserSession> getActiveUserSessions() {
		return userSessionRepository.getActiveUserSessions();
	}

	@Override
	public void setActiveUserSessions(Map<String, UserSession> activeUserSessions) {
		userSessionRepository.setActiveUserSessions(activeUserSessions);
	}
	
	private void alertDestinationOnSubscribe(SimpMessageHeaderAccessor headers) {
		simpMessagingTemplate.convertAndSend(headers.getDestination(), 
				new ChatMessage(HtmlUtils.htmlEscape(
						"---" + headers.getUser().getName() + " has JOINED the room!---")));
	}

	// using UserSession instead of SimpMessageHeaderAccessor like in ...OnSubscribe because on disconnect the event doesn't have a destination
	private void alertDestinationOnDisconnect(UserSession userSession) {
		simpMessagingTemplate.convertAndSend(userSession.getDestination(), 
				new ChatMessage(HtmlUtils.htmlEscape(
						"---" + userSession.getPrincipal().getName() + " has LEFT the room!---")));
	}


}
