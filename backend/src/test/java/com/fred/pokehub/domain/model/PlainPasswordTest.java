package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlainPasswordTest {

	private static final String PASSWORD_REQUIRED = "must not be blank";

	private static final String PASSWORD_LENGTH = "must be between 8 and 72 characters";

	private static final String PASSWORD_COMPLEXITY = "must contain at least one letter and one digit";

	@Test
	void shouldAcceptAPasswordAtTheMinimumLength() {
		assertThat(PlainPassword.requireValid("Pikachu1")).isEqualTo("Pikachu1");
	}

	@Test
	void shouldAcceptAPasswordAtTheMaximumLength() {
		String password = "Aa" + "1".repeat(70);

		assertThat(PlainPassword.requireValid(password)).isEqualTo(password);
	}

	@Test
	void shouldRejectAPasswordShorterThanEightCharacters() {
		assertRejected("Pika#1a", PASSWORD_LENGTH, "Pika#1a");
	}

	@Test
	void shouldRejectAPasswordLongerThanSeventyTwoCharacters() {
		String password = "A1" + "x".repeat(71);

		assertRejected(password, PASSWORD_LENGTH, password);
	}

	@Test
	void shouldRejectALettersOnlyPassword() {
		assertRejected("Pikachuu", PASSWORD_COMPLEXITY, "Pikachuu");
	}

	@Test
	void shouldRejectADigitsOnlyPassword() {
		assertRejected("12345678", PASSWORD_COMPLEXITY, "12345678");
	}

	@Test
	void shouldRejectANullPassword() {
		assertRejected(null, PASSWORD_REQUIRED, null);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "\t", "       "})
	void shouldRejectABlankPassword(String password) {
		assertRejected(password, PASSWORD_REQUIRED, password);
	}

	private static void assertRejected(String password, String expectedMessage, String leakedValue) {
		assertThatThrownBy(() -> PlainPassword.requireValid(password))
				.isInstanceOfSatisfying(InvalidUserException.class, exception -> {
					assertThat(exception.violations())
							.containsExactly(new FieldViolation("password", expectedMessage));
					assertThat(exception.getMessage()).doesNotContain("Pikachu");
					if (leakedValue != null && !leakedValue.isBlank()) {
						assertThat(exception.getMessage()).doesNotContain(leakedValue);
						assertThat(exception.violations().getFirst().message()).doesNotContain(leakedValue);
					}
				});
	}

}
