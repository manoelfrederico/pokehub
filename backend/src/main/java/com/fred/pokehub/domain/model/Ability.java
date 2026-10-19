package com.fred.pokehub.domain.model;

/**
 * A Pokémon ability. {@code hidden} is true for PokéAPI hidden abilities.
 */
public record Ability(String name, boolean hidden) {

	public Ability {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("ability name must not be blank");
		}
		name = name.trim();
	}

}
