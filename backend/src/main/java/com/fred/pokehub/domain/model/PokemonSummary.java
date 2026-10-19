package com.fred.pokehub.domain.model;

import java.util.List;

/**
 * Listing read model for one Pokémon. {@code spriteUrl} and {@code category} may be null.
 * {@code synced} is true when the local replica holds this Pokémon.
 */
public record PokemonSummary(
		int id,
		String name,
		String spriteUrl,
		String category,
		Mass mass,
		List<Ability> abilities,
		boolean synced
) {

	public PokemonSummary(int id, String name, String spriteUrl, String category, Mass mass, List<Ability> abilities) {
		this(id, name, spriteUrl, category, mass, abilities, false);
	}

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

	public PokemonSummary withSynced(boolean synced) {
		if (this.synced == synced) {
			return this;
		}
		return new PokemonSummary(id, name, spriteUrl, category, mass, abilities, synced);
	}

}
