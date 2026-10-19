package com.fred.pokehub.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BcryptPasswordHasherTest {

	private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();

	@Test
	void shouldHashWithoutKeepingThePlainPassword() {
		String hash = hasher.hash("Pikachu#2026");

		assertThat(hash).isNotEqualTo("Pikachu#2026");
		assertThat(hash).doesNotContain("Pikachu#2026");
		assertThat(hasher.matches("Pikachu#2026", hash)).isTrue();
	}

	@Test
	void shouldRejectAWrongPassword() {
		String hash = hasher.hash("Pikachu#2026");

		assertThat(hasher.matches("WrongPass1", hash)).isFalse();
	}

	@Test
	void shouldProduceDifferentHashesForTheSamePassword() {
		String first = hasher.hash("Pikachu#2026");
		String second = hasher.hash("Pikachu#2026");

		assertThat(first).isNotEqualTo(second);
		assertThat(hasher.matches("Pikachu#2026", first)).isTrue();
		assertThat(hasher.matches("Pikachu#2026", second)).isTrue();
	}

	@Test
	void shouldRejectANullPasswordWhenHashing() {
		assertThatThrownBy(() -> hasher.hash(null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectANullPasswordWhenMatching() {
		assertThatThrownBy(() -> hasher.matches(null, hasher.dummyHash()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldExposeADummyHashThatCanBeCompared() {
		assertThat(hasher.matches("unrelated-password-9", hasher.dummyHash())).isFalse();
	}

}
