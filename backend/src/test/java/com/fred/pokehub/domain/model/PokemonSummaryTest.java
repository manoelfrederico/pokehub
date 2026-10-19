package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonSummaryTest {

	@Test
	void shouldExposeListingFields() {
		Mass mass = new Mass(69);
		Ability overgrow = new Ability(65, "overgrow", false);
		PokemonSummary summary = new PokemonSummary(1, "bulbasaur", "https://sprites/1.png", "Seed Pokémon", mass,
				List.of(overgrow));

		assertThat(summary.id()).isEqualTo(1);
		assertThat(summary.name()).isEqualTo("bulbasaur");
		assertThat(summary.spriteUrl()).isEqualTo("https://sprites/1.png");
		assertThat(summary.category()).isEqualTo("Seed Pokémon");
		assertThat(summary.mass()).isEqualTo(mass);
		assertThat(summary.abilities()).containsExactly(overgrow);
		assertThat(summary.synced()).isFalse();
	}

	@Test
	void shouldCopyTheSyncedFlag() {
		PokemonSummary unsynced = new PokemonSummary(1, "bulbasaur", null, null, new Mass(1), List.of(), false);
		PokemonSummary synced = unsynced.withSynced(true);

		assertThat(synced.synced()).isTrue();
		assertThat(synced.id()).isEqualTo(unsynced.id());
		assertThat(synced.name()).isEqualTo(unsynced.name());
		assertThat(synced.mass()).isEqualTo(unsynced.mass());
		assertThat(synced.abilities()).isEqualTo(unsynced.abilities());
		assertThat(unsynced.withSynced(false)).isSameAs(unsynced);
	}

	@Test
	void shouldRejectNonPositiveId() {
		assertThatThrownBy(() -> summary(0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> summary(-1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectBlankName() {
		assertThatThrownBy(() -> new PokemonSummary(1, "  ", null, null, new Mass(1), List.of()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullMass() {
		assertThatThrownBy(() -> new PokemonSummary(1, "bulbasaur", null, null, null, List.of()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullAbilities() {
		assertThatThrownBy(() -> new PokemonSummary(1, "bulbasaur", null, null, new Mass(1), null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldAllowNullSpriteAndNullCategory() {
		PokemonSummary summary = new PokemonSummary(1, "bulbasaur", null, null, new Mass(1), List.of());

		assertThat(summary.spriteUrl()).isNull();
		assertThat(summary.category()).isNull();
	}

	@Test
	void shouldCopyAbilitiesAndExposeAnImmutableList() {
		List<Ability> abilities = new ArrayList<>();
		abilities.add(new Ability(65, "overgrow", false));
		PokemonSummary summary = new PokemonSummary(1, "bulbasaur", null, null, new Mass(1), abilities);

		abilities.add(new Ability(34, "chlorophyll", true));

		assertThat(summary.abilities()).containsExactly(new Ability(65, "overgrow", false));
		assertThatThrownBy(() -> summary.abilities().add(new Ability(34, "chlorophyll", true)))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private static PokemonSummary summary(int id) {
		return new PokemonSummary(id, "bulbasaur", null, null, new Mass(1), List.of());
	}

}
