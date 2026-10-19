package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncResultTest {

	@Test
	void shouldReportWhetherTheSnapshotWasCreated() {
		PokemonView view = new PokemonView(details(), PokemonExtras.empty(), true);

		SyncResult created = new SyncResult(true, view);
		SyncResult refreshed = new SyncResult(false, view);

		assertThat(created.created()).isTrue();
		assertThat(created.view()).isEqualTo(view);
		assertThat(refreshed.created()).isFalse();
		assertThat(refreshed.view()).isEqualTo(view);
	}

	@Test
	void shouldRejectANullView() {
		assertThatThrownBy(() -> new SyncResult(true, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("view");
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
