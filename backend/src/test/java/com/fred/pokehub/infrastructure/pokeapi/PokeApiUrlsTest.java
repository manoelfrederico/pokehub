package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokeApiUrlsTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"https://pokeapi.co/api/v2/pokemon/1/",
			"https://pokeapi.co/api/v2/pokemon/1",
			"https://pokeapi.co/api/v2/pokemon//1/",
			"https://pokeapi.co/api/v2/pokemon-species/1"
	})
	void shouldExtractIdFromResourceUrls(String url) {
		assertThat(PokeApiUrls.extractId(url)).isEqualTo(1);
	}

	@Test
	void shouldExtractIdWhenTheUrlHasNoQueryString() {
		assertThat(PokeApiUrls.extractId("https://pokeapi.co/api/v2/pokemon-species/3")).isEqualTo(3);
	}

	@Test
	void shouldIgnoreAQueryStringWhenExtractingTheId() {
		assertThat(PokeApiUrls.extractId("https://example.invalid/pokemon/25/?foo=1")).isEqualTo(25);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
			"   ",
			"https://pokeapi.co/api/v2/pokemon/abc/",
			"https://pokeapi.co/api/v2/pokemon/0/",
			"https://pokeapi.co/api/v2/pokemon/-4/"
	})
	void shouldRejectUnusableUrls(String url) {
		assertThatThrownBy(() -> PokeApiUrls.extractId(url))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

}
