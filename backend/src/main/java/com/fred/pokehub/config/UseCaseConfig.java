package com.fred.pokehub.config;

import com.fred.pokehub.application.port.in.AuthenticateUserUseCase;
import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.application.port.in.SyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UnsyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasUseCase;
import com.fred.pokehub.application.port.out.AccessTokenIssuer;
import com.fred.pokehub.application.port.out.PasswordHasher;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.application.port.out.UserStore;
import com.fred.pokehub.application.service.AuthenticateUserService;
import com.fred.pokehub.application.service.GetPokemonDetailsService;
import com.fred.pokehub.application.service.ListPokemonService;
import com.fred.pokehub.application.service.RegisterUserService;
import com.fred.pokehub.application.service.SyncPokemonService;
import com.fred.pokehub.application.service.UnsyncPokemonService;
import com.fred.pokehub.application.service.UpdatePokemonExtrasService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class UseCaseConfig {

	@Bean(destroyMethod = "close")
	ExecutorService pokemonCatalogExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}

	@Bean
	ListPokemonUseCase listPokemonUseCase(PokemonCatalog catalog, ExecutorService pokemonCatalogExecutor, PokemonStore store) {
		return new ListPokemonService(catalog, pokemonCatalogExecutor, store);
	}

	@Bean
	GetPokemonDetailsUseCase getPokemonDetailsUseCase(PokemonCatalog catalog, PokemonStore store) {
		return new GetPokemonDetailsService(catalog, store);
	}

	@Bean
	SyncPokemonUseCase syncPokemonUseCase(PokemonCatalog catalog, PokemonStore store) {
		return new SyncPokemonService(catalog, store);
	}

	@Bean
	UnsyncPokemonUseCase unsyncPokemonUseCase(PokemonStore store) {
		return new UnsyncPokemonService(store);
	}

	@Bean
	UpdatePokemonExtrasUseCase updatePokemonExtrasUseCase(PokemonStore store) {
		return new UpdatePokemonExtrasService(store);
	}

	@Bean
	RegisterUserUseCase registerUserUseCase(UserStore store, PasswordHasher hasher) {
		return new RegisterUserService(store, hasher);
	}

	@Bean
	AuthenticateUserUseCase authenticateUserUseCase(UserStore store, PasswordHasher hasher, AccessTokenIssuer issuer) {
		return new AuthenticateUserService(store, hasher, issuer);
	}

}
