package com.dmitriyg.battleship.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.dmitriyg.battleship.model.Role;
import com.dmitriyg.battleship.model.User;
import com.dmitriyg.battleship.model.UserDetailsImpl;
import com.dmitriyg.battleship.repository.RoleRepository;
import com.dmitriyg.battleship.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Override
	public void register(User user) {
		Optional<Role> role = roleRepository.findByName("USER");

		if (role.isPresent()) {
			user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
			user.addRole(role.get());
			user.setEnabled(true);

			userRepository.save(user);
		}

	}
	
	@Override
	public User getCurrentAuthenticatedUser() {
		UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return user.getUser();
	}

	@Override
	public User getById(int id) {
		return userRepository.getReferenceById(id);
	}

}
