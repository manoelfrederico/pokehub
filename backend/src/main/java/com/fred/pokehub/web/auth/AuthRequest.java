package com.fred.pokehub.web.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public record AuthRequest(
		@NotBlank(message = "must not be blank")
		@Size(min = 3, max = 30, message = "must be between 3 and 30 characters")
		@Pattern(regexp = "[A-Za-z0-9._-]+", message = "must contain only letters, digits, dots, underscores and hyphens")
		@Schema(description = "Stored lower case")
		String username,
		@NotBlank(message = "must not be blank")
		@Size(min = 8, max = 72, message = "must be between 8 and 72 characters")
		@Schema(description = "8–72 characters, at least one letter and one digit")
		String password
) {
}
