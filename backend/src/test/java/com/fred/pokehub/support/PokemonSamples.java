package com.fred.pokehub.support;

import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonReference;
import com.fred.pokehub.domain.model.PokemonSummary;

import java.util.List;

public final class PokemonSamples {

	private PokemonSamples() {
	}

	public static PokemonSummary bulbasaur() {
		return new PokemonSummary(
				1,
				"bulbasaur",
				"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
				"Seed Pokémon",
				new Mass(69),
				List.of(new Ability(65, "overgrow", false), new Ability(34, "chlorophyll", true)));
	}

	public static PokemonSummary summary(int id) {
		return new PokemonSummary(id, "pokemon-" + id, null, null, new Mass(10), List.of());
	}

	public static PokemonReference reference(int id) {
		return new PokemonReference(id, "pokemon-" + id);
	}

}
