package com.fred.pokehub.application.service;

import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.PokemonView;

import java.util.Objects;

/**
 * Loads a Pokémon from the catalog. This story never reads the local replica, so the view is unsynced.
 */
public final class GetPokemonDetailsService implements GetPokemonDetailsUseCase {

	private final PokemonCatalog catalog;

	public GetPokemonDetailsService(PokemonCatalog catalog) {
		this.catalog = Objects.requireNonNull(catalog, "catalog");
	}

	@Override
	public PokemonView get(int id) {
		if (id < 1) {
			throw new InvalidPokemonIdException("id must be positive but was " + id);
		}
		return new PokemonView(catalog.findDetailsById(id), null, false);
	}

}
