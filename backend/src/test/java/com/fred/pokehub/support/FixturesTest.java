package com.fred.pokehub.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixturesTest {

	@Test
	void shouldReadClasspathResourceAsUtf8() {
		assertThat(Fixtures.read("support/fixture.txt")).isEqualTo("pokehub-fixture\n");
	}

	@Test
	void shouldFailWhenResourceIsMissing() {
		assertThatThrownBy(() -> Fixtures.read("support/missing.txt"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("support/missing.txt");
	}

}
