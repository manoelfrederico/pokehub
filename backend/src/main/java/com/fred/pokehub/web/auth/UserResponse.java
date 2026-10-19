package com.fred.pokehub.web.auth;

import com.fred.pokehub.domain.model.User;

import java.util.UUID;

public record UserResponse(UUID id, String username) {

	public static UserResponse from(User user) {
		return new UserResponse(user.id(), user.username());
	}

}
