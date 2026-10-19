package com.fred.pokehub.application.exception;

/**
 * The Pokémon is not in the local replica. The web layer maps this to HTTP 404.
 */
public final class PokemonNotSyncedException extends RuntimeException {

	public PokemonNotSyncedException(int id) {
		super("Pokémon " + id + " is not stored.");
	}

}
