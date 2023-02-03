package com.dmitriyg.battleship.model;

import java.security.Principal;

/*
 * Information about the an active user session.
 * i.e. Topic Destinations
 */

public class UserSession {

	private Principal principal;
	private String destination;
	private boolean ready = false;

	public UserSession() {
	}

	public UserSession(Principal principal, String destination) {
		this.principal = principal;
		this.destination = destination;
	}

	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	} 
	
}
