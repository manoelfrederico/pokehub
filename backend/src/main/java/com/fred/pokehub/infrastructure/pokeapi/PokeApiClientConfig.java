package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.port.out.PokemonCatalog;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.util.concurrent.Semaphore;

@Configuration
@EnableConfigurationProperties(PokeApiProperties.class)
public class PokeApiClientConfig {

	public static final String USER_AGENT = "Pokehub/0.0.1 (pokemon-catalog)";

	@Bean
	public RestClient pokeApiRestClient(PokeApiProperties properties) {
		return restClientBuilder(properties).build();
	}

	@Bean
	public Semaphore pokeApiPermits(PokeApiProperties properties) {
		return new Semaphore(properties.maxConcurrentRequests());
	}

	@Bean
	PokeApiEvolutionChains pokeApiEvolutionChains(
			RestClient pokeApiRestClient,
			Semaphore pokeApiPermits,
			PokeApiProperties properties
	) {
		return new PokeApiEvolutionChains(pokeApiRestClient, pokeApiPermits, properties);
	}

	@Bean
	public PokemonCatalog pokeApiPokemonCatalog(
			RestClient pokeApiRestClient,
			Semaphore pokeApiPermits,
			PokeApiProperties properties,
			PokeApiEvolutionChains evolutionChains
	) {
		return new PokeApiPokemonCatalog(pokeApiRestClient, pokeApiPermits, properties, evolutionChains);
	}

	static RestClient.Builder restClientBuilder(PokeApiProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.readTimeout());
		return RestClient.builder()
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
				.configureMessageConverters(converters -> converters.disableDefaults()
						.withJsonConverter(new JacksonJsonHttpMessageConverter(JsonMapper.builder().build())));
	}

}
