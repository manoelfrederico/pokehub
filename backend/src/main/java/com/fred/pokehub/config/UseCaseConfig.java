package com.fred.pokehub.config;

import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.in.SyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UnsyncPokemonUseCase;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.application.service.GetPokemonDetailsService;
import com.fred.pokehub.application.service.ListPokemonService;
import com.fred.pokehub.application.service.SyncPokemonService;
import com.fred.pokehub.application.service.UnsyncPokemonService;
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

}
