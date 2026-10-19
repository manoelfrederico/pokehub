package com.fred.pokehub.domain.model;

import java.util.List;

/**
 * PokéAPI snapshot of one Pokémon. Image, sprite, category and description may be null.
 */
public record PokemonDetails(
		int id,
		String name,
		String imageUrl,
		String spriteUrl,
		String category,
		Mass mass,
		List<Ability> abilities,
		List<Stat> stats,
		String description,
		EvolutionNode evolutionChain
) {

	public PokemonDetails {
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive but was " + id);
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (mass == null) {
			throw new IllegalArgumentException("mass must not be null");
		}
		if (abilities == null) {
			throw new IllegalArgumentException("abilities must not be null");
		}
		if (stats == null) {
			throw new IllegalArgumentException("stats must not be null");
		}
		if (evolutionChain == null) {
			throw new IllegalArgumentException("evolution chain must not be null");
		}
		name = name.trim();
		abilities = List.copyOf(abilities);
		stats = List.copyOf(stats);
	}

}
