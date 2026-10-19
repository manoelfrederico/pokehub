package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.exception.PokemonNotFoundException;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PokeApiPokemonCatalogDetailsTest {

	private static final String BASE = "http://catalog.test/api/v2";

	private static final String POKEMON = BASE + "/pokemon/10033";

	private static final String SPECIES = BASE + "/pokemon-species/3";

	private static final String CHAIN = BASE + "/evolution-chain/1";

	private MockRestServiceServer server;

	private PokeApiPokemonCatalog catalog;

	private RestClient client;

	private PokeApiProperties properties;

	@BeforeEach
	void setUp() {
		properties = new PokeApiProperties(BASE, Duration.ofSeconds(1), Duration.ofSeconds(1), 4);
		RestClient.Builder builder = PokeApiClientConfig.restClientBuilder(properties);
		server = MockRestServiceServer.bindTo(builder).build();
		client = builder.build();
		catalog = catalog(client, new Semaphore(4), properties);
	}

	@Test
	void shouldLoadDetailsWithThreeRequestsInOrder() {
		server.expect(once(), requestTo(POKEMON))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(header(HttpHeaders.USER_AGENT, PokeApiClientConfig.USER_AGENT))
				.andRespond(json(Fixtures.read("pokeapi/pokemon-10033.json")));
		server.expect(once(), requestTo(SPECIES))
				.andRespond(json(Fixtures.read("pokeapi/species-3.json")));
		server.expect(once(), requestTo(CHAIN))
				.andRespond(json(Fixtures.read("pokeapi/evolution-chain-1.json")));

		PokemonDetails details = catalog.findDetailsById(10033);

		assertThat(details.id()).isEqualTo(10033);
		assertThat(details.name()).isEqualTo("venusaur-mega");
		assertThat(details.speciesId()).isEqualTo(3);
		assertThat(details.speciesName()).isEqualTo("venusaur");
		assertThat(details.category()).isEqualTo("Seed Pokémon");
		assertThat(details.evolutionChain().id()).isEqualTo(1);
		assertThat(details.evolutionChain().name()).isEqualTo("bulbasaur");
		server.verify();
	}

	@Test
	void shouldTranslateAPokemonNotFoundIntoPokemonNotFoundException() {
		server.expect(once(), requestTo(POKEMON)).andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(PokemonNotFoundException.class)
				.hasMessageContaining("10033");
		server.verify();
	}

	@Test
	void shouldFailWhenThePokemonCallReturnsServerError() {
		server.expect(requestTo(POKEMON)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		assertUnavailableWithRestClientCause();
	}

	@Test
	void shouldFailWhenThePokemonCallTimesOut() {
		server.expect(requestTo(POKEMON)).andRespond(request -> {
			throw new IOException("timed out");
		});

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.hasCauseInstanceOf(RestClientException.class)
				.hasRootCauseInstanceOf(IOException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"{", ""})
	void shouldFailWhenThePokemonBodyIsUnreadable(String body) {
		server.expect(requestTo(POKEMON)).andRespond(json(body));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {"NOT_FOUND", "INTERNAL_SERVER_ERROR"})
	void shouldFailWhenTheSpeciesCallReturnsAnErrorStatus(String status) {
		expectPokemon();
		server.expect(requestTo(SPECIES)).andRespond(withStatus(HttpStatus.valueOf(status)));

		assertUnavailableWithRestClientCause();
		server.verify();
	}

	@Test
	void shouldFailWhenTheSpeciesCallTimesOut() {
		expectPokemon();
		server.expect(requestTo(SPECIES)).andRespond(request -> {
			throw new IOException("timed out");
		});

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.hasRootCauseInstanceOf(IOException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"{", ""})
	void shouldFailWhenTheSpeciesBodyIsUnreadable(String body) {
		expectPokemon();
		server.expect(requestTo(SPECIES)).andRespond(json(body));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {"NOT_FOUND", "INTERNAL_SERVER_ERROR"})
	void shouldFailWhenTheChainCallReturnsAnErrorStatus(String status) {
		expectPokemon();
		expectSpecies();
		server.expect(requestTo(CHAIN)).andRespond(withStatus(HttpStatus.valueOf(status)));

		assertUnavailableWithRestClientCause();
		server.verify();
	}

	@Test
	void shouldFailWhenTheChainCallTimesOut() {
		expectPokemon();
		expectSpecies();
		server.expect(requestTo(CHAIN)).andRespond(request -> {
			throw new IOException("timed out");
		});

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.hasRootCauseInstanceOf(IOException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"{", ""})
	void shouldFailWhenTheChainBodyIsUnreadable(String body) {
		expectPokemon();
		expectSpecies();
		server.expect(requestTo(CHAIN)).andRespond(json(body));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@Test
	void shouldFailWhenTheEvolutionChainPayloadHasNoLink() {
		expectPokemon();
		expectSpecies();
		server.expect(requestTo(CHAIN)).andRespond(json("{\"chain\":null}"));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@Test
	void shouldFailWhenTheSpeciesHasNoEvolutionChain() {
		expectPokemon();
		server.expect(requestTo(SPECIES)).andRespond(json("{\"id\":3,\"name\":\"venusaur\",\"genera\":[]}"));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
		server.verify();
	}

	@Test
	void shouldFailWhenTheEvolutionChainUrlIsMalformed() {
		expectPokemon();
		server.expect(requestTo(SPECIES))
				.andRespond(json("{\"evolution_chain\":{\"url\":\"https://pokeapi.co/api/v2/evolution-chain/nope\"}}"));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
		server.verify();
	}

	@Test
	void shouldFailWhenThePokemonSpeciesUrlIsMalformed() {
		server.expect(requestTo(POKEMON)).andRespond(json("""
				{"id":10033,"name":"venusaur-mega","weight":1555,"abilities":[],"species":{"name":"venusaur","url":"not-a-url"}}
				"""));

		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
		server.verify();
	}

	@Test
	void shouldReleasePermitsAfterANotFoundDetail() {
		PokeApiPokemonCatalog limited = catalog(client, new Semaphore(1), properties);
		server.expect(org.springframework.test.web.client.ExpectedCount.times(3), requestTo(POKEMON))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
			assertThatThrownBy(() -> limited.findDetailsById(10033)).isInstanceOf(PokemonNotFoundException.class);
			assertThatThrownBy(() -> limited.findDetailsById(10033)).isInstanceOf(PokemonNotFoundException.class);
			assertThatThrownBy(() -> limited.findDetailsById(10033)).isInstanceOf(PokemonNotFoundException.class);
		});
	}

	private void expectPokemon() {
		server.expect(requestTo(POKEMON)).andRespond(json(Fixtures.read("pokeapi/pokemon-10033.json")));
	}

	private void expectSpecies() {
		server.expect(requestTo(SPECIES)).andRespond(json(Fixtures.read("pokeapi/species-3.json")));
	}

	private void assertUnavailableWithRestClientCause() {
		assertThatThrownBy(() -> catalog.findDetailsById(10033))
				.isInstanceOf(CatalogUnavailableException.class)
				.hasCauseInstanceOf(RestClientException.class);
	}

	private static PokeApiPokemonCatalog catalog(RestClient client, Semaphore permits, PokeApiProperties properties) {
		return new PokeApiPokemonCatalog(client, permits, properties, new PokeApiEvolutionChains(client, permits, properties));
	}

	private static ResponseCreator json(String body) {
		return withSuccess(body, MediaType.APPLICATION_JSON);
	}

}
