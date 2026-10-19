package com.fred.pokehub.application.port.in;

import com.fred.pokehub.domain.model.PokemonView;

/**
 * Loads the detail view of one Pokémon.
 */
public interface GetPokemonDetailsUseCase {

	PokemonView get(int id);

}
