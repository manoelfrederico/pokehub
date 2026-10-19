package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoredPokemonTest {

	@Test
	void shouldHoldDetailsAndEmptyExtras() {
		PokemonDetails details = details();
		PokemonExtras extras = PokemonExtras.empty();

		StoredPokemon stored = new StoredPokemon(details, extras);

		assertThat(stored.details()).isEqualTo(details);
		assertThat(stored.extras()).isEqualTo(new PokemonExtras(null, null, List.of()));
	}

	@Test
	void shouldRejectNullDetails() {
		assertThatThrownBy(() -> new StoredPokemon(null, PokemonExtras.empty()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("details");
	}

	@Test
	void shouldRejectNullExtras() {
		assertThatThrownBy(() -> new StoredPokemon(details(), null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("extras");
	}

	private static PokemonDetails details() {
		return new PokemonDetails(
				1,
				"bulbasaur",
				null,
				null,
				null,
				new Mass(69),
				List.of(),
				List.of(),
				null,
				new EvolutionNode(1, "bulbasaur", null, List.of()),
				1,
				"bulbasaur");
	}

}
