package com.dmitriyg.battleship.model;

public class MessagingData {
	
	String type;

	String content;

	public MessagingData() {
	}

	public MessagingData(String type, String content) {
		this.type = type;
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
}
