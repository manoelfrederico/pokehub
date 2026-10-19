package com.fred.pokehub.application.port.out;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessTokenTest {

	@Test
	void shouldKeepAValidToken() {
		AccessToken token = new AccessToken("jwt-value", 3600);

		assertThat(token.token()).isEqualTo("jwt-value");
		assertThat(token.expiresIn()).isEqualTo(3600);
	}

	@Test
	void shouldRejectABlankToken() {
		assertThatThrownBy(() -> new AccessToken("  ", 3600))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("token");
	}

	@Test
	void shouldRejectANonPositiveLifetime() {
		assertThatThrownBy(() -> new AccessToken("jwt-value", 0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("expiresIn");
	}

}
