package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.application.port.out.PasswordHasher;
import com.fred.pokehub.application.port.out.UserStore;
import com.fred.pokehub.domain.exception.InvalidUserException;
import com.fred.pokehub.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserServiceTest {

	@Test
	void shouldStoreAHashAndReturnTheUser() {
		FakeStore store = new FakeStore();
		FakeHasher hasher = new FakeHasher();
		RegisterUserService service = new RegisterUserService(store, hasher);

		User user = service.register("ASH", "Pikachu#2026");

		assertThat(user.username()).isEqualTo("ash");
		assertThat(user.id()).isNotNull();
		assertThat(user.passwordHash()).isEqualTo(FakeHasher.HASH);
		assertThat(user.passwordHash()).doesNotContain("Pikachu#2026");
		assertThat(store.saved.username()).isEqualTo("ash");
		assertThat(store.saved.passwordHash()).isEqualTo(FakeHasher.HASH);
		assertThat(hasher.hashCalls).isEqualTo(1);
		assertThat(hasher.lastPassword).isEqualTo("Pikachu#2026");
	}

	@Test
	void shouldRejectADuplicateUsername() {
		FakeStore store = new FakeStore();
		store.save(new User(UUID.randomUUID(), "ash", "hashed:existing"));
		RegisterUserService service = new RegisterUserService(store, new FakeHasher());

		assertThatThrownBy(() -> service.register("ash", "Pikachu#2026"))
				.isInstanceOf(UsernameAlreadyTakenException.class)
				.hasMessage("Username already taken");
		assertThat(store.saveCalls).isEqualTo(1);
	}

	@Test
	void shouldRejectADuplicateUsernameThatDiffersByCase() {
		FakeStore store = new FakeStore();
		store.save(new User(UUID.randomUUID(), "ash", "hashed:existing"));
		FakeHasher hasher = new FakeHasher();
		RegisterUserService service = new RegisterUserService(store, hasher);

		assertThatThrownBy(() -> service.register("ASH", "Pikachu#2026"))
				.isInstanceOf(UsernameAlreadyTakenException.class);
		assertThat(store.saveCalls).isEqualTo(1);
		assertThat(hasher.hashCalls).isZero();
	}

	@Test
	void shouldStoreNothingWhenTheUsernameIsInvalid() {
		FakeStore store = new FakeStore();
		FakeHasher hasher = new FakeHasher();
		RegisterUserService service = new RegisterUserService(store, hasher);

		assertThatThrownBy(() -> service.register("ab", "Pikachu#2026"))
				.isInstanceOf(InvalidUserException.class);
		assertThat(store.saveCalls).isZero();
		assertThat(hasher.hashCalls).isZero();
	}

	@Test
	void shouldStoreNothingWhenThePasswordIsInvalid() {
		FakeStore store = new FakeStore();
		FakeHasher hasher = new FakeHasher();
		RegisterUserService service = new RegisterUserService(store, hasher);

		assertThatThrownBy(() -> service.register("ash", "short1"))
				.isInstanceOf(InvalidUserException.class);
		assertThat(store.saveCalls).isZero();
		assertThat(hasher.hashCalls).isZero();
	}

	@Test
	void shouldPropagateAStoreFailure() {
		FakeStore store = new FakeStore();
		store.saveFailure = new IllegalStateException("disk");
		RegisterUserService service = new RegisterUserService(store, new FakeHasher());

		assertThatThrownBy(() -> service.register("ash", "Pikachu#2026"))
				.isSameAs(store.saveFailure);
	}

	@Test
	void shouldRejectANullStore() {
		assertThatThrownBy(() -> new RegisterUserService(null, new FakeHasher()))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("store");
	}

	@Test
	void shouldRejectANullHasher() {
		assertThatThrownBy(() -> new RegisterUserService(new FakeStore(), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("hasher");
	}

	private static final class FakeStore implements UserStore {

		private final Map<String, User> users = new LinkedHashMap<>();

		private User saved;

		private int saveCalls;

		private RuntimeException saveFailure;

		@Override
		public Optional<User> findByUsername(String username) {
			return Optional.ofNullable(users.get(username));
		}

		@Override
		public boolean existsByUsername(String username) {
			return users.containsKey(username);
		}

		@Override
		public User save(User user) {
			saveCalls++;
			if (saveFailure != null) {
				throw saveFailure;
			}
			users.put(user.username(), user);
			saved = user;
			return user;
		}

	}

	private static final class FakeHasher implements PasswordHasher {

		private static final String HASH = "$2a$10$not-the-plain-password";

		private int hashCalls;

		private String lastPassword;

		@Override
		public String hash(String password) {
			hashCalls++;
			lastPassword = password;
			return HASH;
		}

		@Override
		public boolean matches(String password, String passwordHash) {
			throw new UnsupportedOperationException("matches");
		}

		@Override
		public String dummyHash() {
			throw new UnsupportedOperationException("dummyHash");
		}

	}

}
