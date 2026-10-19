package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidUserException;

/**
 * Validates a password that has not been hashed. The value is never retained after validation.
 */
public final class PlainPassword {

	public static final int MIN_LENGTH = 8;

	public static final int MAX_LENGTH = 72;

	static final String PASSWORD_REQUIRED = "must not be blank";

	static final String PASSWORD_LENGTH = "must be between 8 and 72 characters";

	static final String PASSWORD_COMPLEXITY = "must contain at least one letter and one digit";

	private PlainPassword() {
	}

	public static String requireValid(String password) {
		if (password == null || password.isBlank()) {
			throw new InvalidUserException(new FieldViolation("password", PASSWORD_REQUIRED));
		}
		if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
			throw new InvalidUserException(new FieldViolation("password", PASSWORD_LENGTH));
		}
		if (!hasLetterAndDigit(password)) {
			throw new InvalidUserException(new FieldViolation("password", PASSWORD_COMPLEXITY));
		}
		return password;
	}

	private static boolean hasLetterAndDigit(String password) {
		boolean letter = false;
		boolean digit = false;
		for (int index = 0; index < password.length(); index++) {
			char character = password.charAt(index);
			letter = letter || Character.isLetter(character);
			digit = digit || Character.isDigit(character);
			if (letter && digit) {
				return true;
			}
		}
		return false;
	}

}
