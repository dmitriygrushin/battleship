package com.dmitriyg.battleship.service;

import com.dmitriyg.battleship.model.User;

public interface UserService {

	void register(User user);

	User getCurrentAuthenticatedUser();

	User getById(int id);

}