package com.fred.pokehub.application.port.out;

/**
 * One-way password hashing. {@code dummyHash} is a valid hash used only to keep login timing stable
 * when the username is unknown.
 */
public interface PasswordHasher {

	String hash(String password);

	boolean matches(String password, String passwordHash);

	String dummyHash();

}
