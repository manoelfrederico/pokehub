package com.fred.pokehub.infrastructure.pokeapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokeApiPropertiesTest {

	@Test
	void shouldKeepAValidConfiguration() {
		PokeApiProperties properties = new PokeApiProperties(
				"  https://pokeapi.co/api/v2  ",
				Duration.ofSeconds(3),
				Duration.ofSeconds(5),
				10);

		assertThat(properties.baseUrl()).isEqualTo("https://pokeapi.co/api/v2");
		assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(3));
		assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(5));
		assertThat(properties.maxConcurrentRequests()).isEqualTo(10);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = "   ")
	void shouldRejectBlankBaseUrl(String baseUrl) {
		assertThatThrownBy(() -> properties(baseUrl, Duration.ofSeconds(1), Duration.ofSeconds(1), 1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullConnectTimeout() {
		assertThatThrownBy(() -> properties("https://pokeapi.co/api/v2", null, Duration.ofSeconds(1), 1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullReadTimeout() {
		assertThatThrownBy(() -> properties("https://pokeapi.co/api/v2", Duration.ofSeconds(1), null, 1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNonPositiveTimeouts() {
		assertThatThrownBy(() -> properties("https://pokeapi.co/api/v2", Duration.ZERO, Duration.ofSeconds(1), 1))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> properties("https://pokeapi.co/api/v2", Duration.ofSeconds(1), Duration.ofSeconds(-1), 1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -1})
	void shouldRejectTooFewConcurrentRequests(int maxConcurrentRequests) {
		assertThatThrownBy(() -> properties("https://pokeapi.co/api/v2", Duration.ofSeconds(1), Duration.ofSeconds(1),
				maxConcurrentRequests))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(Integer.toString(maxConcurrentRequests));
	}

	private static PokeApiProperties properties(String baseUrl, Duration connectTimeout, Duration readTimeout,
			int maxConcurrentRequests) {
		return new PokeApiProperties(baseUrl, connectTimeout, readTimeout, maxConcurrentRequests);
	}

}
