package com.fred.pokehub.application.service;

import com.fred.pokehub.application.port.in.SyncPokemonUseCase;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonView;
import com.fred.pokehub.domain.model.StoredPokemon;
import com.fred.pokehub.domain.model.SyncResult;

import java.util.Objects;

/**
 * Loads the catalog snapshot first, then asks the store to upsert it. The catalog call stays outside the store.
 */
public final class SyncPokemonService implements SyncPokemonUseCase {

	private final PokemonCatalog catalog;

	private final PokemonStore store;

	public SyncPokemonService(PokemonCatalog catalog, PokemonStore store) {
		this.catalog = Objects.requireNonNull(catalog, "catalog");
		this.store = Objects.requireNonNull(store, "store");
	}

	@Override
	public SyncResult sync(int id) {
		if (id < 1) {
			throw new InvalidPokemonIdException("id must be positive but was " + id);
		}
		PokemonDetails details = catalog.findDetailsById(id);
		boolean created = store.saveSnapshot(details);
		StoredPokemon stored = store.findById(id)
				.orElseThrow(() -> new IllegalStateException("Stored Pokémon " + id + " could not be read after sync."));
		return new SyncResult(created, new PokemonView(stored.details(), stored.extras(), true));
	}

}
