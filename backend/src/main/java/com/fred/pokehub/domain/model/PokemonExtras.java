package com.fred.pokehub.domain.model;

import java.util.List;

/**
 * Proprietary fields stored with a synced Pokémon. Validation rules arrive with later stories.
 */
public record PokemonExtras(String localizedName, String region, List<String> tags) {

	public static PokemonExtras empty() {
		return new PokemonExtras(null, null, List.of());
	}

	public PokemonExtras {
		if (tags == null) {
			throw new IllegalArgumentException("tags must not be null");
		}
		tags = List.copyOf(tags);
	}

}
