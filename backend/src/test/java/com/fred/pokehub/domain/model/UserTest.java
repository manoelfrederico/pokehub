package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

	private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private static final String HASH = "hashed-password";

	private static final String USERNAME_REQUIRED = "must not be blank";

	private static final String USERNAME_LENGTH = "must be between 3 and 30 characters";

	private static final String USERNAME_CHARACTERS = "must contain only lowercase letters, digits, dots, underscores and hyphens";

	@Test
	void shouldAcceptAUsernameAtTheMinimumLength() {
		User user = new User(ID, "ash", HASH);

		assertThat(user.username()).isEqualTo("ash");
		assertThat(user.id()).isEqualTo(ID);
		assertThat(user.passwordHash()).isEqualTo(HASH);
	}

	@Test
	void shouldAcceptAUsernameAtTheMaximumLength() {
		String username = "a".repeat(30);

		User user = new User(ID, username, HASH);

		assertThat(user.username()).isEqualTo(username);
	}

	@Test
	void shouldRejectAUsernameShorterThanThreeCharacters() {
		assertThatThrownBy(() -> new User(ID, "ab", HASH))
				.isInstanceOfSatisfying(InvalidUserException.class, exception ->
						assertThat(exception.violations())
								.containsExactly(new FieldViolation("username", USERNAME_LENGTH)));
	}

	@Test
	void shouldRejectAUsernameLongerThanThirtyCharacters() {
		assertThatThrownBy(() -> new User(ID, "a".repeat(31), HASH))
				.isInstanceOfSatisfying(InvalidUserException.class, exception ->
						assertThat(exception.violations())
								.containsExactly(new FieldViolation("username", USERNAME_LENGTH)));
	}

	@Test
	void shouldNormalizeAnUpperCaseUsername() {
		User user = new User(ID, "ASH", HASH);

		assertThat(user.username()).isEqualTo("ash");
	}

	@Test
	void shouldTrimAndNormalizeAUsername() {
		User user = new User(ID, "  Ash.Ketchum_1-2  ", HASH);

		assertThat(user.username()).isEqualTo("ash.ketchum_1-2");
	}

	@ParameterizedTest
	@ValueSource(strings = {"ash!", "as h", "ash@", "ash/ketchum", "ashé"})
	void shouldRejectAUsernameWithDisallowedCharacters(String username) {
		assertThatThrownBy(() -> new User(ID, username, HASH))
				.isInstanceOfSatisfying(InvalidUserException.class, exception ->
						assertThat(exception.violations())
								.containsExactly(new FieldViolation("username", USERNAME_CHARACTERS)));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"  ", "\t", "\n"})
	void shouldRejectABlankUsername(String username) {
		assertThatThrownBy(() -> new User(ID, username, HASH))
				.isInstanceOfSatisfying(InvalidUserException.class, exception ->
						assertThat(exception.violations())
								.containsExactly(new FieldViolation("username", USERNAME_REQUIRED)));
	}

	@Test
	void shouldRejectANullId() {
		assertThatThrownBy(() -> new User(null, "ash", HASH))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("id");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"  "})
	void shouldRejectABlankPasswordHash(String passwordHash) {
		assertThatThrownBy(() -> new User(ID, "ash", passwordHash))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("passwordHash");
	}

	@Test
	void shouldNormalizeAUsernameWithoutBuildingAUser() {
		assertThat(User.normalizeUsername("  ASH  ")).isEqualTo("ash");
	}

}
