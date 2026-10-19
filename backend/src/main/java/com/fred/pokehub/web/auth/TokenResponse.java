package com.fred.pokehub.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
		String accessToken,
		String tokenType,
		@Schema(description = "Lifetime in seconds") long expiresIn) {
}
