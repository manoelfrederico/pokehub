package com.fred.pokehub.application.exception;

/**
 * The chosen username is already registered. The web layer maps this to HTTP 409.
 */
public final class UsernameAlreadyTakenException extends RuntimeException {

	public UsernameAlreadyTakenException(String username) {
		super("Username already taken");
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("username must not be blank");
		}
	}

}
