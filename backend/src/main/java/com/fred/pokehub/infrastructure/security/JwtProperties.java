package com.fred.pokehub.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "pokehub.jwt")
public record JwtProperties(String secret, Duration ttl) {

	public static final int MIN_SECRET_BYTES = 32;

	public JwtProperties {
		if (secret == null || secret.isBlank()) {
			throw new IllegalArgumentException("secret must not be blank");
		}
		if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
			throw new IllegalArgumentException("secret must be at least 32 bytes");
		}
		if (ttl == null || ttl.isZero() || ttl.isNegative()) {
			throw new IllegalArgumentException("ttl must be positive");
		}
	}

	SecretKey secretKey() {
		return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

}
