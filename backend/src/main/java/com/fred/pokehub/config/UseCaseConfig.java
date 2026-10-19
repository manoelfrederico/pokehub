package com.fred.pokehub.config;

import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.service.GetPokemonDetailsService;
import com.fred.pokehub.application.service.ListPokemonService;
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
	ListPokemonUseCase listPokemonUseCase(PokemonCatalog catalog, ExecutorService pokemonCatalogExecutor) {
		return new ListPokemonService(catalog, pokemonCatalogExecutor);
	}

	@Bean
	GetPokemonDetailsUseCase getPokemonDetailsUseCase(PokemonCatalog catalog) {
		return new GetPokemonDetailsService(catalog);
	}

}
