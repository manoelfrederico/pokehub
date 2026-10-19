package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonPageTest {

	@Test
	void shouldPointAtTheNextPageWhenTheCatalogContinues() {
		PokemonPage page = page(1351, 20, 0, summary(1));

		assertThat(page.nextOffset()).contains(20);
		assertThat(page.previousOffset()).isEmpty();
	}

	@Test
	void shouldExposeBothOffsetsOnAMiddlePage() {
		PokemonPage page = page(1351, 20, 40, summary(1));

		assertThat(page.nextOffset()).contains(60);
		assertThat(page.previousOffset()).contains(20);
	}

	@Test
	void shouldOmitNextOnTheLastPartialPage() {
		PokemonPage page = page(1351, 20, 1340, summary(1));

		assertThat(page.nextOffset()).isEmpty();
		assertThat(page.previousOffset()).contains(1320);
	}

	@Test
	void shouldOmitNextWhenOffsetPlusLimitEqualsCount() {
		PokemonPage page = page(100, 20, 80, summary(1));

		assertThat(page.nextOffset()).isEmpty();
	}

	@Test
	void shouldIncludeNextWhenOffsetPlusLimitEqualsCountMinusOne() {
		PokemonPage page = page(100, 20, 79, summary(1));

		assertThat(page.nextOffset()).contains(99);
	}

	@Test
	void shouldClampPreviousOffsetToZeroWhenOffsetIsBelowLimit() {
		PokemonPage page = page(100, 20, 10, summary(1));

		assertThat(page.previousOffset()).contains(0);
		assertThat(page.nextOffset()).contains(30);
	}

	@Test
	void shouldOmitBothLinksForAnEmptyCatalog() {
		PokemonPage page = new PokemonPage(0, PageQuery.of(20, 0), List.of());

		assertThat(page.nextOffset()).isEmpty();
		assertThat(page.previousOffset()).isEmpty();
	}

	@Test
	void shouldOmitNextAndKeepPreviousWhenOffsetIsBeyondCount() {
		PokemonPage page = page(10, 20, 50);

		assertThat(page.nextOffset()).isEmpty();
		assertThat(page.previousOffset()).contains(30);
	}

	@Test
	void shouldNotOverflowWhenOffsetIsMaxInteger() {
		PokemonPage page = new PokemonPage(Integer.MAX_VALUE, new PageQuery(20, Integer.MAX_VALUE), List.of());

		assertThat(page.nextOffset()).isEmpty();
		assertThat(page.previousOffset()).contains(Integer.MAX_VALUE - 20);
	}

	@Test
	void shouldRejectNegativeCount() {
		assertThatThrownBy(() -> new PokemonPage(-1, PageQuery.of(20, 0), List.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectNullItems() {
		assertThatThrownBy(() -> new PokemonPage(0, PageQuery.of(20, 0), null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullQuery() {
		assertThatThrownBy(() -> new PokemonPage(0, null, List.of()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldCopyItemsAndExposeAnImmutableList() {
		List<PokemonSummary> items = new ArrayList<>();
		items.add(summary(1));
		PokemonPage page = new PokemonPage(1, PageQuery.of(20, 0), items);

		items.add(summary(2));

		assertThat(page.items()).containsExactly(summary(1));
		assertThatThrownBy(() -> page.items().add(summary(3)))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private static PokemonPage page(int count, int limit, int offset, PokemonSummary... items) {
		return new PokemonPage(count, PageQuery.of(limit, offset), List.of(items));
	}

	private static PokemonSummary summary(int id) {
		return new PokemonSummary(id, "pokemon-" + id, null, null, new Mass(1), List.of());
	}

}
