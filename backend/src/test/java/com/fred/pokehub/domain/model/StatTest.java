package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatTest {

	@Test
	void shouldKeepATrimmedNameAndBaseValue() {
		Stat stat = new Stat("  hp  ", 45);

		assertThat(stat.name()).isEqualTo("hp");
		assertThat(stat.baseValue()).isEqualTo(45);
	}

	@Test
	void shouldAllowAZeroBaseValue() {
		assertThat(new Stat("hp", 0).baseValue()).isZero();
	}

	@Test
	void shouldRejectANegativeBaseValue() {
		assertThatThrownBy(() -> new Stat("hp", -1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectNullName() {
		assertThatThrownBy(() -> new Stat(null, 1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankName() {
		assertThatThrownBy(() -> new Stat("   ", 1))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
