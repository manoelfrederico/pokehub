package com.fred.pokehub.config;

import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Objects;

/**
 * Seeds the documented demo account through registration so the password is hashed by the use case.
 */
public final class DemoUserSeeder implements ApplicationRunner {

	private final RegisterUserUseCase registerUser;

	private final DemoUserProperties properties;

	public DemoUserSeeder(RegisterUserUseCase registerUser, DemoUserProperties properties) {
		this.registerUser = Objects.requireNonNull(registerUser, "registerUser");
		this.properties = Objects.requireNonNull(properties, "properties");
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.enabled()) {
			return;
		}
		try {
			registerUser.register(properties.username(), properties.password());
		}
		catch (UsernameAlreadyTakenException ignored) {
			// Already present from a previous start or a previous run of this seeder.
		}
	}

}
