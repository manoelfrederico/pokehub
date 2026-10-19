package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidUserException;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Registered account. Usernames are stored in lower case; the password hash is never a plain password.
 */
public record User(UUID id, String username, String passwordHash) {

	public static final int MIN_USERNAME_LENGTH = 3;

	public static final int MAX_USERNAME_LENGTH = 30;

	static final String USERNAME_REQUIRED = "must not be blank";

	static final String USERNAME_LENGTH = "must be between 3 and 30 characters";

	static final String USERNAME_CHARACTERS = "must contain only lowercase letters, digits, dots, underscores and hyphens";

	private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z0-9._-]+");

	public User {
		Objects.requireNonNull(id, "id");
		username = normalizeUsername(username);
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new IllegalArgumentException("passwordHash must not be blank");
		}
	}

	public static String normalizeUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new InvalidUserException(new FieldViolation("username", USERNAME_REQUIRED));
		}
		String normalized = username.strip().toLowerCase(Locale.ROOT);
		if (normalized.length() < MIN_USERNAME_LENGTH || normalized.length() > MAX_USERNAME_LENGTH) {
			throw new InvalidUserException(new FieldViolation("username", USERNAME_LENGTH));
		}
		if (!USERNAME_PATTERN.matcher(normalized).matches()) {
			throw new InvalidUserException(new FieldViolation("username", USERNAME_CHARACTERS));
		}
		return normalized;
	}

}
