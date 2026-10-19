package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonViewTest {

	@Test
	void shouldRepresentAnUnsyncedPokemonWithoutExtras() {
		PokemonDetails details = details();
		PokemonView view = new PokemonView(details, null, false);

		assertThat(view.details()).isEqualTo(details);
		assertThat(view.extras()).isNull();
		assertThat(view.synced()).isFalse();
	}

	@Test
	void shouldRepresentASyncedPokemonWithExtras() {
		PokemonExtras extras = new PokemonExtras("Bulbassauro", "Kanto", List.of("starter"));
		PokemonView view = new PokemonView(details(), extras, true);

		assertThat(view.extras()).isEqualTo(extras);
		assertThat(view.synced()).isTrue();
	}

	@Test
	void shouldRejectSyncedWithoutExtras() {
		assertThatThrownBy(() -> new PokemonView(details(), null, true))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectUnsyncedWithExtras() {
		PokemonExtras extras = new PokemonExtras(null, null, List.of());

		assertThatThrownBy(() -> new PokemonView(details(), extras, false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullDetails() {
		assertThatThrownBy(() -> new PokemonView(null, null, false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldCopyTags() {
		List<String> tags = new java.util.ArrayList<>();
		tags.add("starter");
		PokemonExtras extras = new PokemonExtras("Bulbassauro", "Kanto", tags);

		tags.add("grass");

		assertThat(extras.tags()).containsExactly("starter");
		assertThatThrownBy(() -> extras.tags().add("grass"))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void shouldRejectNullTags() {
		assertThatThrownBy(() -> new PokemonExtras(null, null, null))
				.isInstanceOf(IllegalArgumentException.class);
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
