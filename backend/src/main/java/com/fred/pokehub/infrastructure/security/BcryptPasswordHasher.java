package com.fred.pokehub.infrastructure.security;

import com.fred.pokehub.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt hasher. {@code dummyHash} is encoded once at class load so missing-user logins still run {@code matches}.
 */
@Component
public class BcryptPasswordHasher implements PasswordHasher {

	private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

	private static final String DUMMY_HASH = ENCODER.encode("pokehub-missing-user-timing-guard");

	@Override
	public String hash(String password) {
		return ENCODER.encode(requirePassword(password));
	}

	@Override
	public boolean matches(String password, String passwordHash) {
		return ENCODER.matches(requirePassword(password), passwordHash);
	}

	@Override
	public String dummyHash() {
		return DUMMY_HASH;
	}

	private static String requirePassword(String password) {
		if (password == null) {
			throw new IllegalArgumentException("password must not be null");
		}
		return password;
	}

}
