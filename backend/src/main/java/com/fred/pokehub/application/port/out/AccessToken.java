package com.fred.pokehub.application.port.out;

/**
 * Issued bearer token. {@code expiresIn} is the lifetime in seconds.
 */
public record AccessToken(String token, long expiresIn) {

	public AccessToken {
		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("token must not be blank");
		}
		if (expiresIn < 1) {
			throw new IllegalArgumentException("expiresIn must be positive");
		}
	}

}
