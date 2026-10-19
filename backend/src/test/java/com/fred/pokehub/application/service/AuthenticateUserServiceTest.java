package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.InvalidCredentialsException;
import com.fred.pokehub.application.port.out.AccessToken;
import com.fred.pokehub.application.port.out.AccessTokenIssuer;
import com.fred.pokehub.application.port.out.PasswordHasher;
import com.fred.pokehub.application.port.out.UserStore;
import com.fred.pokehub.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticateUserServiceTest {

	private static final User ASH = new User(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"ash",
			"hashed:Pikachu#2026");

	@Test
	void shouldIssueATokenForAValidUser() {
		FakeStore store = new FakeStore();
		store.add(ASH);
		FakeHasher hasher = new FakeHasher();
		FakeIssuer issuer = new FakeIssuer();
		AuthenticateUserService service = new AuthenticateUserService(store, hasher, issuer);

		AccessToken token = service.authenticate("ash", "Pikachu#2026");

		assertThat(token).isEqualTo(new AccessToken("token-for-ash", 3600));
		assertThat(issuer.lastUser).isEqualTo(ASH);
		assertThat(hasher.matchCalls).isEqualTo(1);
		assertThat(hasher.lastHash).isEqualTo(ASH.passwordHash());
	}

	@Test
	void shouldMatchUsernamesIgnoringCase() {
		FakeStore store = new FakeStore();
		store.add(ASH);
		FakeIssuer issuer = new FakeIssuer();
		AuthenticateUserService service = new AuthenticateUserService(store, new FakeHasher(), issuer);

		AccessToken token = service.authenticate("ASH", "Pikachu#2026");

		assertThat(token.token()).isEqualTo("token-for-ash");
		assertThat(issuer.lastUser).isEqualTo(ASH);
	}

	@Test
	void shouldRejectAWrongPasswordWithTheSameFailureAsAnUnknownUser() {
		FakeStore store = new FakeStore();
		store.add(ASH);
		FakeHasher hasher = new FakeHasher();
		AuthenticateUserService wrongPassword = new AuthenticateUserService(store, hasher, new FakeIssuer());

		Throwable wrong = catchCredentials(() -> wrongPassword.authenticate("ash", "WrongPass1"));

		FakeHasher unknownHasher = new FakeHasher();
		AuthenticateUserService unknownUser = new AuthenticateUserService(new FakeStore(), unknownHasher, new FakeIssuer());
		Throwable unknown = catchCredentials(() -> unknownUser.authenticate("ash", "Pikachu#2026"));

		assertThat(wrong).isInstanceOf(InvalidCredentialsException.class);
		assertThat(unknown.getClass()).isEqualTo(wrong.getClass());
		assertThat(unknown.getMessage()).isEqualTo(wrong.getMessage());
		assertThat(hasher.matchCalls).isEqualTo(1);
		assertThat(unknownHasher.matchCalls).isEqualTo(1);
		assertThat(unknownHasher.lastHash).isEqualTo(unknownHasher.dummyHash());
		assertThat(hasher.lastHash).isEqualTo(ASH.passwordHash());
	}

	@Test
	void shouldPropagateATokenIssuerFailure() {
		FakeStore store = new FakeStore();
		store.add(ASH);
		FakeIssuer issuer = new FakeIssuer();
		issuer.failure = new IllegalStateException("signer");
		AuthenticateUserService service = new AuthenticateUserService(store, new FakeHasher(), issuer);

		assertThatThrownBy(() -> service.authenticate("ash", "Pikachu#2026"))
				.isSameAs(issuer.failure);
	}

	@Test
	void shouldRejectNullCollaborators() {
		FakeStore store = new FakeStore();
		FakeHasher hasher = new FakeHasher();
		FakeIssuer issuer = new FakeIssuer();

		assertThatThrownBy(() -> new AuthenticateUserService(null, hasher, issuer))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("store");
		assertThatThrownBy(() -> new AuthenticateUserService(store, null, issuer))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("hasher");
		assertThatThrownBy(() -> new AuthenticateUserService(store, hasher, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("issuer");
	}

	private static Throwable catchCredentials(Runnable action) {
		try {
			action.run();
			throw new AssertionError("expected InvalidCredentialsException");
		}
		catch (InvalidCredentialsException exception) {
			return exception;
		}
	}

	private static final class FakeStore implements UserStore {

		private final Map<String, User> users = new LinkedHashMap<>();

		private void add(User user) {
			users.put(user.username(), user);
		}

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
			users.put(user.username(), user);
			return user;
		}

	}

	private static final class FakeHasher implements PasswordHasher {

		private int matchCalls;

		private String lastHash;

		@Override
		public String hash(String password) {
			throw new UnsupportedOperationException("hash");
		}

		@Override
		public boolean matches(String password, String passwordHash) {
			matchCalls++;
			lastHash = passwordHash;
			return ("hashed:" + password).equals(passwordHash);
		}

		@Override
		public String dummyHash() {
			return "hashed:dummy-timing-guard";
		}

	}

	private static final class FakeIssuer implements AccessTokenIssuer {

		private User lastUser;

		private RuntimeException failure;

		@Override
		public AccessToken issue(User user) {
			if (failure != null) {
				throw failure;
			}
			lastUser = user;
			return new AccessToken("token-for-" + user.username(), 3600);
		}

	}

}
