package com.dmitriyg.battleship.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmitriyg.battleship.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String username);
}
