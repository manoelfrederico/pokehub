package com.fred.pokehub.domain.model;

import java.math.BigDecimal;

/**
 * Weight stored in hectograms, the unit returned by PokéAPI.
 */
public record Mass(int hectograms) {

	public Mass {
		if (hectograms < 0) {
			throw new IllegalArgumentException("hectograms must be >= 0 but was " + hectograms);
		}
	}

	/**
	 * @return mass in kilograms, with one decimal place
	 */
	public BigDecimal kilograms() {
		return BigDecimal.valueOf(hectograms, 1);
	}

}
