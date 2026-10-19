package com.fred.pokehub.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsPropertiesTest {

	@Test
	void shouldKeepExplicitOrigins() {
		CorsProperties properties = new CorsProperties(List.of("http://localhost:5173"));

		assertThat(properties.allowedOrigins()).containsExactly("http://localhost:5173");
	}

	@Test
	void shouldRejectEmptyOrigins() {
		assertThatThrownBy(() -> new CorsProperties(List.of()))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new CorsProperties(null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectWildcardOrigins() {
		assertThatThrownBy(() -> new CorsProperties(List.of("*")))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new CorsProperties(List.of("https://*.example")))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
