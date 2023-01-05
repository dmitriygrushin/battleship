package com.dmitriyg.battleship.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.MessagingData;

@Controller
public class WebSocketController {
	
	@Autowired
	// used instead of @SendTo so that I could have dynamic rooms 
	// (@SendTo("/room/tempRoom") // where client subscribes to)
	private SimpMessagingTemplate simpMessagingTemplate; 
	

	@MessageMapping("message/{roomId}") // client uses: "/battleship/message" to send data
	public void messageReceiveSend(MessagingData message, @DestinationVariable int roomId, Principal principal) {
		simpMessagingTemplate.convertAndSend("/room/" + roomId,
			new MessagingData("chat-message", HtmlUtils.htmlEscape(principal.getName() + ": " + message.getContent())));
	}


}
