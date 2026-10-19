package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.domain.model.Ability;

import java.util.ArrayList;
import java.util.List;

final class AbilityMapper {

	private AbilityMapper() {
	}

	static List<Ability> toAbilities(List<PokeApiResponses.AbilitySlot> slots) {
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
