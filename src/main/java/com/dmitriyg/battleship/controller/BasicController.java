package com.dmitriyg.battleship.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BasicController {
	
	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/room")
	public String test() {
		return "room";
	}
}
