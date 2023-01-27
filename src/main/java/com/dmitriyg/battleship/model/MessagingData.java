package com.dmitriyg.battleship.model;

public class MessagingData<T> {
	
	String type;

	T content;

	public MessagingData() {
	}

	public MessagingData(String type, T content) {
		this.type = type;
		this.content = content;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
}
