package com.fred.pokehub.domain.model;

import java.util.List;

/**
 * A page of catalog references plus the upstream total.
 */
public record PokemonReferencePage(int count, List<PokemonReference> items) {

	public PokemonReferencePage {
		if (count < 0) {
			throw new IllegalArgumentException("count must be >= 0 but was " + count);
		}
		if (items == null) {
			throw new IllegalArgumentException("items must not be null");
		}
		items = List.copyOf(items);
	}

}
