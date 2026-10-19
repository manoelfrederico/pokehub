package com.fred.pokehub.domain.exception;

/**
 * Raised when a Pokémon id is not a positive integer.
 */
public final class InvalidPokemonIdException extends RuntimeException {

	public InvalidPokemonIdException(String message) {
		super(message);
	}

}
