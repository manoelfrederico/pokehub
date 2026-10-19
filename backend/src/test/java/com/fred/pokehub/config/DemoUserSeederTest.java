package com.fred.pokehub.config;

import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidUserException;
import com.fred.pokehub.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoUserSeederTest {

	@Test
	void shouldCreateTheDemoUserOnce() {
		FakeRegister register = new FakeRegister();
		DemoUserSeeder seeder = new DemoUserSeeder(register, enabled("ash", "Pikachu#2026"));

		seeder.run(new DefaultApplicationArguments());

		assertThat(register.calls).isEqualTo(1);
		assertThat(register.username).isEqualTo("ash");
		assertThat(register.password).isEqualTo("Pikachu#2026");
	}

	@Test
	void shouldDoNothingWhenTheUserAlreadyExists() {
		FakeRegister register = new FakeRegister();
		DemoUserSeeder seeder = new DemoUserSeeder(register, enabled("ash", "Pikachu#2026"));

		seeder.run(new DefaultApplicationArguments());
		register.failure = new UsernameAlreadyTakenException("ash");
		seeder.run(new DefaultApplicationArguments());

		assertThat(register.calls).isEqualTo(2);
		assertThat(register.created).isEqualTo(1);
	}

	@Test
	void shouldDoNothingWhenDisabled() {
		FakeRegister register = new FakeRegister();
		DemoUserSeeder seeder = new DemoUserSeeder(register, new DemoUserProperties(false, "ash", "Pikachu#2026"));

		seeder.run(new DefaultApplicationArguments());

		assertThat(register.calls).isZero();
	}

	@Test
	void shouldPropagateFailuresOtherThanAlreadyTaken() {
		FakeRegister register = new FakeRegister();
		register.failure = new InvalidUserException(new FieldViolation("password", "must not be blank"));
		DemoUserSeeder seeder = new DemoUserSeeder(register, enabled("ash", "bad"));

		assertThatThrownBy(() -> seeder.run(new DefaultApplicationArguments()))
				.isSameAs(register.failure);
	}

	private static DemoUserProperties enabled(String username, String password) {
		return new DemoUserProperties(true, username, password);
	}

	private static final class FakeRegister implements RegisterUserUseCase {

		private int calls;

		private int created;

		private String username;

		private String password;

		private RuntimeException failure;

		@Override
		public User register(String username, String password) {
			calls++;
			this.username = username;
			this.password = password;
			if (failure != null) {
				throw failure;
			}
			created++;
			return new User(UUID.randomUUID(), username, "hashed");
		}

	}

}
