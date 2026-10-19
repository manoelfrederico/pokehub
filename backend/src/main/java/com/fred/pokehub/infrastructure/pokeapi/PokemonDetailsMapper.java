package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.Stat;

import java.util.ArrayList;
import java.util.List;

final class PokemonDetailsMapper {

	private PokemonDetailsMapper() {
	}

	static PokemonDetails toDetails(
			PokeApiResponses.Pokemon pokemon,
			PokeApiResponses.Species species,
			EvolutionNode evolutionChain
	) {
		return new PokemonDetails(
				pokemon.id(),
				pokemon.name(),
				SpriteResolver.detailImage(pokemon.sprites()).orElse(null),
				SpriteResolver.listingSprite(pokemon.sprites()).orElse(null),
				EnglishGenus.category(species),
				new Mass(pokemon.weight()),
				AbilityMapper.toAbilities(pokemon.abilities()),
				stats(pokemon.stats()),
				DescriptionCleaner.englishDescription(species == null ? null : species.flavorTextEntries()),
				evolutionChain);
	}

	private static List<Stat> stats(List<PokeApiResponses.StatSlot> slots) {
		if (slots == null) {
			return List.of();
		}
		List<Stat> stats = new ArrayList<>();
		for (PokeApiResponses.StatSlot slot : slots) {
			if (named(slot)) {
				stats.add(new Stat(slot.stat().name(), slot.baseStat()));
			}
		}
		return List.copyOf(stats);
	}

	private static boolean named(PokeApiResponses.StatSlot slot) {
		return slot != null
				&& slot.stat() != null
				&& slot.stat().name() != null
				&& !slot.stat().name().isBlank();
	}

}
