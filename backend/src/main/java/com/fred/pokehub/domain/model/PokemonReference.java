package com.fred.pokehub.domain.model;

/**
 * A Pokémon named in a catalog page, before its summary is loaded.
 */
public record PokemonReference(int id, String name) {

	public PokemonReference {
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive but was " + id);
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		name = name.trim();
	}

}
