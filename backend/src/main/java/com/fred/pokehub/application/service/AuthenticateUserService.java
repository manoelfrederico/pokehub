package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.InvalidCredentialsException;
import com.fred.pokehub.application.port.in.AuthenticateUserUseCase;
import com.fred.pokehub.application.port.out.AccessToken;
import com.fred.pokehub.application.port.out.AccessTokenIssuer;
import com.fred.pokehub.application.port.out.PasswordHasher;
import com.fred.pokehub.application.port.out.UserStore;
import com.fred.pokehub.domain.model.User;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Verifies credentials with a constant-time hash comparison, including when the user is missing.
 */
public final class AuthenticateUserService implements AuthenticateUserUseCase {

	private final UserStore store;

	private final PasswordHasher hasher;

	private final AccessTokenIssuer issuer;

	public AuthenticateUserService(UserStore store, PasswordHasher hasher, AccessTokenIssuer issuer) {
		this.store = Objects.requireNonNull(store, "store");
		this.hasher = Objects.requireNonNull(hasher, "hasher");
		this.issuer = Objects.requireNonNull(issuer, "issuer");
	}

	@Override
	public AccessToken authenticate(String username, String password) {
		Optional<User> found = store.findByUsername(normalizedLookup(username));
		String hash = found.map(User::passwordHash).orElseGet(hasher::dummyHash);
		if (!hasher.matches(password, hash) || found.isEmpty()) {
			throw new InvalidCredentialsException();
		}
		return issuer.issue(found.get());
	}

	private static String normalizedLookup(String username) {
		if (username == null) {
			return "";
		}
		return username.strip().toLowerCase(Locale.ROOT);
	}

}
