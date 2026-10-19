package com.fred.pokehub.infrastructure.pokeapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "pokehub.pokeapi")
public record PokeApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout, int maxConcurrentRequests) {

	public PokeApiProperties {
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new IllegalArgumentException("baseUrl must not be blank");
		}
		if (connectTimeout == null) {
			throw new IllegalArgumentException("connectTimeout must not be null");
		}
		if (readTimeout == null) {
			throw new IllegalArgumentException("readTimeout must not be null");
		}
		if (connectTimeout.isZero() || connectTimeout.isNegative()) {
			throw new IllegalArgumentException("connectTimeout must be positive");
		}
		if (readTimeout.isZero() || readTimeout.isNegative()) {
			throw new IllegalArgumentException("readTimeout must be positive");
		}
		if (maxConcurrentRequests < 1) {
			throw new IllegalArgumentException("maxConcurrentRequests must be >= 1 but was " + maxConcurrentRequests);
		}
		baseUrl = baseUrl.trim();
	}

}
