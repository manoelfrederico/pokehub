package com.fred.pokehub.application.exception;

/**
 * Login failed. The same message is used for an unknown user and a wrong password.
 */
public final class InvalidCredentialsException extends RuntimeException {

	public static final String MESSAGE = "Invalid username or password.";

	public InvalidCredentialsException() {
		super(MESSAGE);
	}

}
