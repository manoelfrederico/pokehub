package com.fred.pokehub.domain.model;

/**
 * A Pokémon ability. {@code id} is the PokéAPI ability id. {@code hidden} is true for hidden abilities.
 */
public record Ability(int id, String name, boolean hidden) {

	public Ability {
		if (id <= 0) {
			throw new IllegalArgumentException("ability id must be positive but was " + id);
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("ability name must not be blank");
		}
		name = name.trim();
	}

}
