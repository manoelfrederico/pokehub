package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.PokemonNotSyncedException;
import com.fred.pokehub.application.port.in.UnsyncPokemonUseCase;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;

import java.util.Objects;

/**
 * Deletes the local replica of one Pokémon.
 */
public final class UnsyncPokemonService implements UnsyncPokemonUseCase {

	private final PokemonStore store;

	public UnsyncPokemonService(PokemonStore store) {
		this.store = Objects.requireNonNull(store, "store");
	}

	@Override
	public void unsync(int id) {
		if (id < 1) {
			throw new InvalidPokemonIdException("id must be positive but was " + id);
		}
		if (!store.deleteById(id)) {
			throw new PokemonNotSyncedException(id);
		}
	}

}
