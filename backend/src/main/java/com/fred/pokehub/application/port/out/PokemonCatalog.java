package com.fred.pokehub.application.port.out;

import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;

/**
 * Read-only gateway to the upstream Pokémon catalog.
 */
public interface PokemonCatalog {

	PokemonReferencePage listReferences(PageQuery query);

	PokemonSummary findSummaryById(int id);

	PokemonDetails findDetailsById(int id);

}
