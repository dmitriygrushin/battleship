package com.dmitriyg.battleship.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.dmitriyg.battleship.model.ChatMessage;

@Controller
public class WebSocketController {
	
	@Autowired
	// used instead of @SendTo so that I could have dynamic rooms 
	// (@SendTo("/room/tempRoom") // where client subscribes to)
	private SimpMessagingTemplate simpMessagingTemplate; 

	@MessageMapping("message/{roomId}") // client uses: "/battleship/message" to send data
	public void messageReceiveSend(ChatMessage message, @DestinationVariable int roomId) {
		this.simpMessagingTemplate.convertAndSend("/room/" + roomId,
			new ChatMessage(HtmlUtils.htmlEscape(message.getMessage())));
		
	}

}
