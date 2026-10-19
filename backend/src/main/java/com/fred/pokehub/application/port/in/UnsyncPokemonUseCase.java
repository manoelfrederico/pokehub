package com.fred.pokehub.application.port.in;

/**
 * Removes one Pokémon from the local store.
 */
public interface UnsyncPokemonUseCase {

	void unsync(int id);

}
