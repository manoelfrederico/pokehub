package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 30)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	UUID id() {
		return id;
	}

	void setId(UUID id) {
		this.id = id;
	}

	String username() {
		return username;
	}

	void setUsername(String username) {
		this.username = username;
	}

	String passwordHash() {
		return passwordHash;
	}

	void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

}
