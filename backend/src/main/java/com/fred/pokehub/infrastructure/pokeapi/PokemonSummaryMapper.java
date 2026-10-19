package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonSummary;

final class PokemonSummaryMapper {

	private PokemonSummaryMapper() {
	}

	static PokemonSummary toSummary(PokeApiResponses.Pokemon pokemon, PokeApiResponses.Species species) {
		return new PokemonSummary(
				pokemon.id(),
				pokemon.name(),
				SpriteResolver.listingSprite(pokemon.sprites()).orElse(null),
				EnglishGenus.category(species),
				new Mass(pokemon.weight()),
				AbilityMapper.toAbilities(pokemon.abilities()));
	}

}
