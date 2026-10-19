package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbilityTest {

	@Test
	void shouldKeepATrimmedNameAndHiddenFlag() {
		Ability ability = new Ability(65, "  overgrow  ", true);

		assertThat(ability.id()).isEqualTo(65);
		assertThat(ability.name()).isEqualTo("overgrow");
		assertThat(ability.hidden()).isTrue();
	}

	@Test
	void shouldRejectANonPositiveId() {
		assertThatThrownBy(() -> new Ability(0, "overgrow", false))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> new Ability(-1, "overgrow", false))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectNullName() {
		assertThatThrownBy(() -> new Ability(65, null, false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectBlankName() {
		assertThatThrownBy(() -> new Ability(65, "   ", false))
				.isInstanceOf(IllegalArgumentException.class);
	}

}
