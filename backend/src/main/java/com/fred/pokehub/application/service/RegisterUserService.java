package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.application.port.out.PasswordHasher;
import com.fred.pokehub.application.port.out.UserStore;
import com.fred.pokehub.domain.model.PlainPassword;
import com.fred.pokehub.domain.model.User;

import java.util.Objects;
import java.util.UUID;

/**
 * Validates credentials, hashes the password and stores a new user.
 */
public final class RegisterUserService implements RegisterUserUseCase {

	private final UserStore store;

	private final PasswordHasher hasher;

	public RegisterUserService(UserStore store, PasswordHasher hasher) {
		this.store = Objects.requireNonNull(store, "store");
		this.hasher = Objects.requireNonNull(hasher, "hasher");
	}

	@Override
	public User register(String username, String password) {
		PlainPassword.requireValid(password);
		String normalized = User.normalizeUsername(username);
		if (store.existsByUsername(normalized)) {
			throw new UsernameAlreadyTakenException(normalized);
		}
		return store.save(new User(UUID.randomUUID(), normalized, hasher.hash(password)));
	}

}
