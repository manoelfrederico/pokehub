package com.fred.pokehub.domain.exception;

import java.util.List;

/**
 * Raised when proprietary Pokémon fields break a domain rule. The web layer maps this to HTTP 400.
 */
public final class InvalidPokemonExtrasException extends RuntimeException {

	private final List<FieldViolation> violations;

	public InvalidPokemonExtrasException(List<FieldViolation> violations) {
		super("Pokémon extras are invalid.");
		if (violations == null || violations.isEmpty()) {
			throw new IllegalArgumentException("violations must not be empty");
		}
		this.violations = List.copyOf(violations);
	}

	public List<FieldViolation> violations() {
		return violations;
	}

}
