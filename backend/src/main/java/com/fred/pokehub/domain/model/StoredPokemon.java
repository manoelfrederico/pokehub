package com.fred.pokehub.domain.model;

/**
 * A Pokémon replicated locally: the PokéAPI snapshot plus proprietary extras.
 * Extras are always present; an unsynced view is not a stored Pokémon.
 */
public record StoredPokemon(PokemonDetails details, PokemonExtras extras) {

	public StoredPokemon {
		if (details == null) {
			throw new IllegalArgumentException("details must not be null");
		}
		if (extras == null) {
			throw new IllegalArgumentException("extras must not be null");
		}
	}

}
