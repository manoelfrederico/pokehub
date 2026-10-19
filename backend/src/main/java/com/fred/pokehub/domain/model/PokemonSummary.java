package com.fred.pokehub.domain.model;

import java.util.List;

/**
 * Listing read model for one Pokémon. {@code spriteUrl} and {@code category} may be null.
 */
public record PokemonSummary(
		int id,
		String name,
		String spriteUrl,
		String category,
		Mass mass,
		List<Ability> abilities
) {

	public PokemonSummary {
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive but was " + id);
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (mass == null) {
			throw new IllegalArgumentException("mass must not be null");
		}
		if (abilities == null) {
			throw new IllegalArgumentException("abilities must not be null");
		}
		name = name.trim();
		abilities = List.copyOf(abilities);
	}

}
