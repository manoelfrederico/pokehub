package com.fred.pokehub.application.port.in;

import com.fred.pokehub.domain.model.PokemonView;

/**
 * Replaces the proprietary fields of a Pokémon that is already stored locally.
 */
public interface UpdatePokemonExtrasUseCase {

	PokemonView update(UpdatePokemonExtrasCommand command);

}
