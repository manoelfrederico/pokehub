package com.fred.pokehub.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * One page of Pokémon summaries. Link offsets follow the API contract and use long arithmetic.
 */
public record PokemonPage(int count, PageQuery query, List<PokemonSummary> items) {

	public PokemonPage {
		if (count < 0) {
			throw new IllegalArgumentException("count must be >= 0 but was " + count);
		}
		if (query == null) {
			throw new IllegalArgumentException("query must not be null");
		}
		if (items == null) {
			throw new IllegalArgumentException("items must not be null");
		}
		items = List.copyOf(items);
	}

	public Optional<Integer> nextOffset() {
		long end = (long) query.offset() + query.limit();
		if (end >= count) {
			return Optional.empty();
		}
		return Optional.of((int) end);
	}

	public Optional<Integer> previousOffset() {
		if (query.offset() == 0) {
			return Optional.empty();
		}
		long previous = (long) query.offset() - query.limit();
		if (previous < 0) {
			return Optional.of(0);
		}
		return Optional.of((int) previous);
	}

}
