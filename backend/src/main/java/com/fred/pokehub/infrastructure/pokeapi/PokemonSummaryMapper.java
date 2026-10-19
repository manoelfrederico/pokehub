package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonSummary;

import java.util.ArrayList;
import java.util.List;

final class PokemonSummaryMapper {

	private static final String ENGLISH = "en";

	private PokemonSummaryMapper() {
	}

	static PokemonSummary toSummary(PokeApiResponses.Pokemon pokemon, PokeApiResponses.Species species) {
		return new PokemonSummary(
				pokemon.id(),
				pokemon.name(),
				SpriteResolver.listingSprite(pokemon.sprites()).orElse(null),
				category(species),
				new Mass(pokemon.weight()),
				abilities(pokemon.abilities()));
	}

	private static String category(PokeApiResponses.Species species) {
		if (species == null || species.genera() == null) {
			return null;
		}
		for (PokeApiResponses.Genus genus : species.genera()) {
			if (isEnglish(genus)) {
				return genus.genus();
			}
		}
		return null;
	}

	private static boolean isEnglish(PokeApiResponses.Genus genus) {
		return genus != null && genus.language() != null && ENGLISH.equals(genus.language().name());
	}

	private static List<Ability> abilities(List<PokeApiResponses.AbilitySlot> slots) {
		if (slots == null) {
			return List.of();
		}
		List<Ability> abilities = new ArrayList<>();
		for (PokeApiResponses.AbilitySlot slot : slots) {
			if (named(slot)) {
				abilities.add(new Ability(slot.ability().name(), slot.hidden()));
			}
		}
		return List.copyOf(abilities);
	}

	private static boolean named(PokeApiResponses.AbilitySlot slot) {
		return slot != null
				&& slot.ability() != null
				&& slot.ability().name() != null
				&& !slot.ability().name().isBlank();
	}

}
