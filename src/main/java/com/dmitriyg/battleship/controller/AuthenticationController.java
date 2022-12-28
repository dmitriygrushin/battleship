package com.dmitriyg.battleship.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmitriyg.battleship.model.User;
import com.dmitriyg.battleship.service.UserService;

@Controller
public class AuthenticationController {
	
	@Autowired 
	private UserService userService;

	@GetMapping("/register")
	public String registrationForm(Model model) {
		model.addAttribute("user", new User());
		return "authentication/register";
	}

	@GetMapping("/login")
	public String loginForm() {
		return "authentication/login";
	}
	
	@PostMapping("/register")
	public String register(User user) {
		userService.register(user);
		return "redirect:/";
	}

}
