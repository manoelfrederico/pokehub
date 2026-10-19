package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonReferenceTest {

	@Test
	void shouldKeepAPositiveIdAndTrimmedName() {
		PokemonReference reference = new PokemonReference(1, "  bulbasaur  ");

		assertThat(reference.id()).isEqualTo(1);
		assertThat(reference.name()).isEqualTo("bulbasaur");
	}

	@Test
	void shouldRejectNonPositiveId() {
		assertThatThrownBy(() -> new PokemonReference(0, "bulbasaur"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> new PokemonReference(-1, "bulbasaur"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectNullOrBlankName() {
		assertThatThrownBy(() -> new PokemonReference(1, null))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new PokemonReference(1, "  "))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
