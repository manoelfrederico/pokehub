package com.fred.pokehub.domain.exception;

import java.util.List;

/**
 * Raised when a username or password breaks a domain rule. The web layer maps this to HTTP 400.
 */
public final class InvalidUserException extends RuntimeException {

	private final List<FieldViolation> violations;

	public InvalidUserException(List<FieldViolation> violations) {
		super("User credentials are invalid.");
		if (violations == null || violations.isEmpty()) {
			throw new IllegalArgumentException("violations must not be empty");
		}
		this.violations = List.copyOf(violations);
	}

	public InvalidUserException(FieldViolation violation) {
		this(List.of(violation));
	}

	public List<FieldViolation> violations() {
		return violations;
	}

}
