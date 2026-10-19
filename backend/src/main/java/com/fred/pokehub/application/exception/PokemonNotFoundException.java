package com.fred.pokehub.application.exception;

/**
 * The requested Pokémon does not exist in the upstream catalog. The web layer maps this to HTTP 404.
 */
public final class PokemonNotFoundException extends RuntimeException {

	public PokemonNotFoundException(int id) {
		super("Pokémon " + id + " was not found.");
	}

}
