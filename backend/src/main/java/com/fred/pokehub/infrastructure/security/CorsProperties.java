package com.fred.pokehub.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "pokehub.cors")
public record CorsProperties(List<String> allowedOrigins) {

	public CorsProperties {
		if (allowedOrigins == null || allowedOrigins.isEmpty()) {
			throw new IllegalArgumentException("allowedOrigins must not be empty");
		}
		allowedOrigins = List.copyOf(allowedOrigins);
		for (String origin : allowedOrigins) {
			if (origin == null || origin.isBlank() || origin.contains("*")) {
				throw new IllegalArgumentException("allowedOrigins must be explicit origins without wildcards");
			}
		}
	}

}
