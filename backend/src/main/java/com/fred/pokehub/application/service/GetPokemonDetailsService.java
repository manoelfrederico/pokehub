package com.fred.pokehub.application.service;

import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.PokemonView;

import java.util.Objects;

/**
 * Serves a stored replica when one exists, otherwise loads the catalog. The view is unsynced only for catalog reads.
 */
public final class GetPokemonDetailsService implements GetPokemonDetailsUseCase {

	private final PokemonCatalog catalog;

	private final PokemonStore store;

	public GetPokemonDetailsService(PokemonCatalog catalog, PokemonStore store) {
		this.catalog = Objects.requireNonNull(catalog, "catalog");
		this.store = Objects.requireNonNull(store, "store");
	}

	@Override
	public PokemonView get(int id) {
		if (id < 1) {
			throw new InvalidPokemonIdException("id must be positive but was " + id);
		}
		return store.findById(id)
				.map(stored -> new PokemonView(stored.details(), stored.extras(), true))
				.orElseGet(() -> new PokemonView(catalog.findDetailsById(id), null, false));
	}

}
