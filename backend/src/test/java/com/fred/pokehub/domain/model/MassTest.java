package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MassTest {

	@Test
	void shouldConvertSixtyNineHectogramsToSixPointNineKilograms() {
		assertThat(new Mass(69).kilograms()).isEqualTo(new BigDecimal("6.9"));
	}

	@Test
	void shouldConvertZeroHectogramsToZeroKilograms() {
		assertThat(new Mass(0).kilograms()).isEqualTo(new BigDecimal("0.0"));
	}

	@Test
	void shouldConvertOneHectogramToOneTenthOfAKilogram() {
		assertThat(new Mass(1).kilograms()).isEqualTo(new BigDecimal("0.1"));
	}

	@Test
	void shouldConvertLargeHectogramValuesWithoutOverflow() {
		assertThat(new Mass(Integer.MAX_VALUE).kilograms())
				.isEqualTo(BigDecimal.valueOf(Integer.MAX_VALUE, 1));
	}

	@Test
	void shouldRejectNegativeHectograms() {
		assertThatThrownBy(() -> new Mass(-1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldBeEqualWhenHectogramsMatch() {
		assertThat(new Mass(69)).isEqualTo(new Mass(69)).isNotEqualTo(new Mass(70));
	}

}
