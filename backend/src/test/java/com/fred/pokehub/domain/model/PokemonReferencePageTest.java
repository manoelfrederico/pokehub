package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonReferencePageTest {

	@Test
	void shouldExposeCountAndItems() {
		PokemonReference reference = new PokemonReference(1, "bulbasaur");
		PokemonReferencePage page = new PokemonReferencePage(1351, List.of(reference));

		assertThat(page.count()).isEqualTo(1351);
		assertThat(page.items()).containsExactly(reference);
	}

	@Test
	void shouldRejectNegativeCount() {
		assertThatThrownBy(() -> new PokemonReferencePage(-1, List.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectNullItems() {
		assertThatThrownBy(() -> new PokemonReferencePage(0, null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldCopyItemsAndExposeAnImmutableList() {
		List<PokemonReference> items = new ArrayList<>();
		items.add(new PokemonReference(1, "bulbasaur"));
		PokemonReferencePage page = new PokemonReferencePage(1351, items);

		items.clear();

		assertThat(page.items()).containsExactly(new PokemonReference(1, "bulbasaur"));
		assertThatThrownBy(() -> page.items().add(new PokemonReference(2, "ivysaur")))
				.isInstanceOf(UnsupportedOperationException.class);
	}

}
