package com.fred.pokehub.domain.model;

/**
 * One core stat. {@code baseValue} is the PokéAPI base stat, never negative.
 */
public record Stat(String name, int baseValue) {

	public Stat {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("stat name must not be blank");
		}
		if (baseValue < 0) {
			throw new IllegalArgumentException("base value must be >= 0 but was " + baseValue);
		}
		name = name.trim();
	}

}
