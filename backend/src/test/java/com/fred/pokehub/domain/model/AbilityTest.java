package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbilityTest {

	@Test
	void shouldKeepATrimmedNameAndHiddenFlag() {
		Ability ability = new Ability("  overgrow  ", true);

		assertThat(ability.name()).isEqualTo("overgrow");
		assertThat(ability.hidden()).isTrue();
	}

	@Test
	void shouldRejectNullName() {
		assertThatThrownBy(() -> new Ability(null, false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankName() {
		assertThatThrownBy(() -> new Ability("   ", false))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
