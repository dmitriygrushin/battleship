package com.dmitriyg.battleship.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.MessagingData;
import com.dmitriyg.battleship.service.UserSessionService;
import com.dmitriyg.battleship.util.WebSocketUtils;

@Controller
public class WebSocketController {
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate; 
	
	@Autowired
	private UserSessionService userSessionService;
	
	@Autowired
	private WebSocketUtils webSocketUtils;

	@MessageMapping("message/{roomId}") // client uses: "/app/message" to send data
	public void messageReceiveSend(MessagingData<String> message, @DestinationVariable int roomId, Principal principal) {
		simpMessagingTemplate.convertAndSend("/topic/" + roomId,
			new MessagingData<>("chat-message", HtmlUtils.htmlEscape(principal.getName() + ": " + message.getContent())));
	}

	@MessageMapping("ready/{roomId}") 
	public void clientReady(@Header("simpSessionId") String sessionId, MessagingData<String> message, @DestinationVariable int roomId, Principal principal) {
		// update the user's isReady field
		userSessionService.find(sessionId).setReady(true);
		simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/topic/" + roomId, 
				new MessagingData<>("ready-success", "ready successful"));

		if (webSocketUtils.isTopicReady("/topic/" + roomId)) {
			simpMessagingTemplate.convertAndSend("/topic/" + roomId,
				new MessagingData<>("ready-room-success", "Room is ready!"));

			// Game Loop - #1
			// for the sake of simplicity the first user to get ready will go first
			webSocketUtils.broadcastToTopic(principal.getName(), "/topic/" + roomId, 
					new MessagingData<>("ready-room-battle", ""));
		}
	}
	
	// Game Loop - #3
	@MessageMapping("coordinates/{roomId}")
	public void receiveCoordinates(MessagingData<String> message, @DestinationVariable int roomId, Principal principal) {
		webSocketUtils.broadcastToTopic(principal.getName(), "/topic/" + roomId, 
				new MessagingData<>("battle-coordinates", message.getContent()));
	}

	// Game Loop - #5
	@MessageMapping("hit/{roomId}")
	public void hitResponse(MessagingData<String> message, @DestinationVariable int roomId, Principal principal) {
		webSocketUtils.broadcastToTopic(principal.getName(), "/topic/" + roomId, 
				new MessagingData<>("battle-coordinates-hit", message.getContent()));
	}

	@MessageMapping("miss/{roomId}")
	public void missResponse(MessagingData<String> message, @DestinationVariable int roomId, Principal principal) {
		webSocketUtils.broadcastToTopic(principal.getName(), "/topic/" + roomId, 
				new MessagingData<>("battle-coordinates-miss", message.getContent()));
	}

	// Game Loop - END
	@MessageMapping("finish/{roomId}")
	public void finishResponse(MessagingData<String> message, @DestinationVariable int roomId, Principal principal) {
		webSocketUtils.broadcastToTopic(principal.getName(), "/topic/" + roomId, 
				new MessagingData<>("battle-finish", message.getContent()));
	}
	
	

}
