package com.fred.pokehub.application.port.in;

import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonPage;

/**
 * Lists one page of Pokémon summaries.
 */
public interface ListPokemonUseCase {

	PokemonPage list(PageQuery query);

}
