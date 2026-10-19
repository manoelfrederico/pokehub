package com.fred.pokehub.application.port.in;

import com.fred.pokehub.domain.model.User;

/**
 * Registers a new user after validating credentials and hashing the password.
 */
public interface RegisterUserUseCase {

	User register(String username, String password);

}
