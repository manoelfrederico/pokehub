package com.fred.pokehub.domain.model;

/**
 * Detail read model. {@code synced} is true exactly when {@code extras} is present.
 */
public record PokemonView(PokemonDetails details, PokemonExtras extras, boolean synced) {

	public PokemonView {
		if (details == null) {
			throw new IllegalArgumentException("details must not be null");
		}
		if (synced != (extras != null)) {
			throw new IllegalArgumentException("synced must be true exactly when extras are present");
		}
	}

}
