package com.fred.pokehub.application.port.out;

import com.fred.pokehub.domain.model.User;

import java.util.Optional;

/**
 * Persistence for registered users. Usernames are stored in lower case.
 */
public interface UserStore {

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	User save(User user);

}
