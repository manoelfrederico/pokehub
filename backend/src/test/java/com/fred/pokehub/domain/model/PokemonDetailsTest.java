package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonDetailsTest {

	@Test
	void shouldExposeTheSnapshot() {
		PokemonDetails details = bulbasaur();

		assertThat(details.id()).isEqualTo(1);
		assertThat(details.name()).isEqualTo("bulbasaur");
		assertThat(details.imageUrl()).isEqualTo("https://images/1.png");
		assertThat(details.spriteUrl()).isEqualTo("https://sprites/1.png");
		assertThat(details.category()).isEqualTo("Seed Pokémon");
		assertThat(details.mass()).isEqualTo(new Mass(69));
		assertThat(details.abilities()).containsExactly(new Ability("overgrow", false));
		assertThat(details.stats()).containsExactly(new Stat("hp", 45));
		assertThat(details.description()).isEqualTo("A strange seed.");
		assertThat(details.evolutionChain().name()).isEqualTo("bulbasaur");
	}

	@Test
	void shouldTrimTheName() {
		assertThat(details(1, "  bulbasaur  ").name()).isEqualTo("bulbasaur");
	}

	@Test
	void shouldAllowNullImagesCategoryAndDescription() {
		PokemonDetails details = new PokemonDetails(
				1,
				"bulbasaur",
				null,
				null,
				null,
				new Mass(69),
				List.of(),
				List.of(),
				null,
				chain());

		assertThat(details.imageUrl()).isNull();
		assertThat(details.spriteUrl()).isNull();
		assertThat(details.category()).isNull();
		assertThat(details.description()).isNull();
		assertThat(details.abilities()).isEmpty();
		assertThat(details.stats()).isEmpty();
	}

	@Test
	void shouldAllowBlankImagesCategoryAndDescription() {
		PokemonDetails details = new PokemonDetails(
				1,
				"bulbasaur",
				" ",
				" ",
				" ",
				new Mass(69),
				List.of(),
				List.of(),
				" ",
				chain());

		assertThat(details.imageUrl()).isEqualTo(" ");
		assertThat(details.spriteUrl()).isEqualTo(" ");
		assertThat(details.category()).isEqualTo(" ");
		assertThat(details.description()).isEqualTo(" ");
	}

	@Test
	void shouldRejectNonPositiveId() {
		assertThatThrownBy(() -> details(0, "bulbasaur"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> details(-1, "bulbasaur"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectBlankName() {
		assertThatThrownBy(() -> details(1, "  "))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullName() {
		assertThatThrownBy(() -> details(1, null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullMass() {
		assertThatThrownBy(() -> new PokemonDetails(1, "bulbasaur", null, null, null, null, List.of(), List.of(), null, chain()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullAbilities() {
		assertThatThrownBy(() -> new PokemonDetails(1, "bulbasaur", null, null, null, new Mass(1), null, List.of(), null, chain()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullStats() {
		assertThatThrownBy(() -> new PokemonDetails(1, "bulbasaur", null, null, null, new Mass(1), List.of(), null, null, chain()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullEvolutionChain() {
		assertThatThrownBy(() -> new PokemonDetails(1, "bulbasaur", null, null, null, new Mass(1), List.of(), List.of(), null, null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldCopyAbilitiesAndStats() {
		List<Ability> abilities = new ArrayList<>();
		abilities.add(new Ability("overgrow", false));
		List<Stat> stats = new ArrayList<>();
		stats.add(new Stat("hp", 45));
		PokemonDetails details = new PokemonDetails(
				1, "bulbasaur", null, null, null, new Mass(1), abilities, stats, null, chain());

		abilities.add(new Ability("chlorophyll", true));
		stats.add(new Stat("attack", 49));

		assertThat(details.abilities()).containsExactly(new Ability("overgrow", false));
		assertThat(details.stats()).containsExactly(new Stat("hp", 45));
		assertThatThrownBy(() -> details.abilities().add(new Ability("chlorophyll", true)))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> details.stats().add(new Stat("attack", 49)))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private static PokemonDetails bulbasaur() {
		return new PokemonDetails(
				1,
				"bulbasaur",
				"https://images/1.png",
				"https://sprites/1.png",
				"Seed Pokémon",
				new Mass(69),
				List.of(new Ability("overgrow", false)),
				List.of(new Stat("hp", 45)),
				"A strange seed.",
				chain());
	}

	private static PokemonDetails details(int id, String name) {
		return new PokemonDetails(id, name, null, null, null, new Mass(1), List.of(), List.of(), null, chain());
	}

	private static EvolutionNode chain() {
		return new EvolutionNode(1, "bulbasaur", null, List.of());
	}

}
