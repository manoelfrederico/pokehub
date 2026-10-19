package com.fred.pokehub.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtPropertiesTest {

	private static final String SECRET_32 = "a".repeat(32);

	@Test
	void shouldAcceptAThirtyTwoByteSecretAndAPositiveTtl() {
		JwtProperties properties = new JwtProperties(SECRET_32, Duration.ofHours(1));

		assertThat(properties.secret()).isEqualTo(SECRET_32);
		assertThat(properties.secret().getBytes(StandardCharsets.UTF_8)).hasSize(32);
		assertThat(properties.ttl()).isEqualTo(Duration.ofHours(1));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"  ", "\t"})
	void shouldRejectAMissingOrBlankSecret(String secret) {
		assertThatThrownBy(() -> new JwtProperties(secret, Duration.ofHours(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("secret");
	}

	@Test
	void shouldRejectAThirtyOneByteSecret() {
		assertThatThrownBy(() -> new JwtProperties("a".repeat(31), Duration.ofHours(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("32");
	}

	@Test
	void shouldRejectANonPositiveTtl() {
		assertThatThrownBy(() -> new JwtProperties(SECRET_32, Duration.ZERO))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ttl");
		assertThatThrownBy(() -> new JwtProperties(SECRET_32, Duration.ofSeconds(-1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ttl");
		assertThatThrownBy(() -> new JwtProperties(SECRET_32, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ttl");
	}

}
