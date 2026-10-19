package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonReference;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;
import com.fred.pokehub.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PokeApiPokemonCatalogTest {

	private static final String BASE = "http://catalog.test/api/v2";

	private static final PageQuery FIRST_PAGE = PageQuery.of(2, 0);

	private MockRestServiceServer server;

	private PokeApiProperties properties;

	private RestClient client;

	private PokeApiPokemonCatalog catalog;

	@BeforeEach
	void setUp() {
		properties = new PokeApiProperties(BASE, Duration.ofSeconds(1), Duration.ofSeconds(1), 4);
		RestClient.Builder builder = PokeApiClientConfig.restClientBuilder(properties);
		server = MockRestServiceServer.bindTo(builder).build();
		client = builder.build();
		catalog = new PokeApiPokemonCatalog(client, new Semaphore(4), properties);
	}

	@Test
	void shouldListReferencesFromTheConfiguredBaseUrl() {
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(header(HttpHeaders.USER_AGENT, PokeApiClientConfig.USER_AGENT))
				.andRespond(jsonBody(Fixtures.read("pokeapi/list-first-page.json")));

		PokemonReferencePage page = catalog.listReferences(FIRST_PAGE);

		assertThat(page.count()).isEqualTo(1351);
		assertThat(page.items()).containsExactly(
				new PokemonReference(1, "bulbasaur"),
				new PokemonReference(2, "ivysaur"),
				new PokemonReference(3, "venusaur"));
		server.verify();
	}

	@Test
	void shouldReturnNoReferencesWhenResultsAreEmpty() {
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andRespond(jsonBody("{\"count\":1351,\"results\":[]}"));

		PokemonReferencePage page = catalog.listReferences(FIRST_PAGE);

		assertThat(page.count()).isEqualTo(1351);
		assertThat(page.items()).isEmpty();
	}

	@Test
	void shouldReturnNoReferencesWhenResultsAreNull() {
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andRespond(jsonBody("{\"count\":1351,\"results\":null}"));

		PokemonReferencePage page = catalog.listReferences(FIRST_PAGE);

		assertThat(page.count()).isEqualTo(1351);
		assertThat(page.items()).isEmpty();
	}

	@Test
	void shouldRejectAListThatIsMissingCount() {
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andRespond(jsonBody("{\"results\":[]}"));

		assertThatThrownBy(() -> catalog.listReferences(FIRST_PAGE))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@Test
	void shouldStripATrailingSlashFromTheBaseUrl() {
		PokeApiProperties slashed = new PokeApiProperties(BASE + "/", Duration.ofSeconds(1), Duration.ofSeconds(1), 4);
		PokeApiPokemonCatalog slashedCatalog = new PokeApiPokemonCatalog(client, new Semaphore(1), slashed);
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andRespond(jsonBody("{\"count\":1,\"results\":[]}"));

		assertThat(slashedCatalog.listReferences(FIRST_PAGE).count()).isEqualTo(1);
	}

	@Test
	void shouldLoadASummaryUsingTheSpeciesIdRatherThanThePayloadHost() {
		server.expect(requestTo(BASE + "/pokemon/1"))
				.andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(header(HttpHeaders.USER_AGENT, PokeApiClientConfig.USER_AGENT))
				.andRespond(jsonBody(Fixtures.read("pokeapi/pokemon-1.json")));
		server.expect(requestTo(BASE + "/pokemon-species/1"))
				.andRespond(jsonBody(Fixtures.read("pokeapi/species-1.json")));

		PokemonSummary summary = catalog.findSummaryById(1);

		assertThat(summary.id()).isEqualTo(1);
		assertThat(summary.name()).isEqualTo("bulbasaur");
		assertThat(summary.category()).isEqualTo("Seed Pokémon");
		assertThat(summary.mass()).isEqualTo(new Mass(69));
		assertThat(summary.abilities()).containsExactly(
				new Ability("overgrow", false),
				new Ability("chlorophyll", true));
		server.verify();
	}

	@Test
	void shouldFailWhenTheUpstreamPokemonIsNotFound() {
		expectPokemonStatus(HttpStatus.NOT_FOUND);

		assertUnavailableWithRestClientCause();
	}

	@Test
	void shouldFailWhenTheUpstreamPokemonErrors() {
		expectPokemonStatus(HttpStatus.INTERNAL_SERVER_ERROR);

		assertUnavailableWithRestClientCause();
	}

	@Test
	void shouldFailWhenTheUpstreamCallThrowsIoException() {
		server.expect(requestTo(BASE + "/pokemon/1")).andRespond(request -> {
			throw new IOException("timed out");
		});

		assertThatThrownBy(() -> catalog.findSummaryById(1))
				.isInstanceOf(CatalogUnavailableException.class)
				.hasCauseInstanceOf(RestClientException.class)
				.hasRootCauseInstanceOf(IOException.class);
	}

	@Test
	void shouldFailWhenThePokemonPayloadIsMalformed() {
		server.expect(requestTo(BASE + "/pokemon/1")).andRespond(jsonBody("{"));

		assertUnavailableWithRestClientCause();
	}

	@Test
	void shouldFailWhenThePokemonBodyIsEmpty() {
		server.expect(requestTo(BASE + "/pokemon/1")).andRespond(jsonBody(""));

		assertThatThrownBy(() -> catalog.findSummaryById(1))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@Test
	void shouldFailWhenThePokemonPayloadIsMissingSpecies() {
		server.expect(requestTo(BASE + "/pokemon/1"))
				.andRespond(jsonBody("{\"id\":1,\"name\":\"bulbasaur\",\"weight\":69,\"abilities\":[]}"));

		assertThatThrownBy(() -> catalog.findSummaryById(1))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
		server.verify();
	}

	@Test
	void shouldFailWhenTheSpeciesCallFails() {
		server.expect(requestTo(BASE + "/pokemon/1")).andRespond(jsonBody(Fixtures.read("pokeapi/pokemon-1.json")));
		server.expect(requestTo(BASE + "/pokemon-species/1")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		assertUnavailableWithRestClientCause();
		server.verify();
	}

	@Test
	void shouldRejectAMalformedEmbeddedUrl() {
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andRespond(jsonBody("{\"count\":1,\"results\":[{\"name\":\"broken\",\"url\":\"https://pokeapi.co/api/v2/pokemon/nope\"}]}"));

		assertThatThrownBy(() -> catalog.listReferences(FIRST_PAGE))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@Test
	void shouldReleasePermitsAfterRepeatedFailures() {
		PokeApiPokemonCatalog limited = new PokeApiPokemonCatalog(client, new Semaphore(1), properties);
		server.expect(times(3), requestTo(BASE + "/pokemon/1")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
			assertThatThrownBy(() -> limited.findSummaryById(1)).isInstanceOf(CatalogUnavailableException.class);
			assertThatThrownBy(() -> limited.findSummaryById(1)).isInstanceOf(CatalogUnavailableException.class);
			assertThatThrownBy(() -> limited.findSummaryById(1)).isInstanceOf(CatalogUnavailableException.class);
		});
	}

	@Test
	void shouldRestoreTheInterruptFlagWhenPermitAcquisitionIsInterrupted() {
		Semaphore permits = new Semaphore(0);
		PokeApiPokemonCatalog blocked = new PokeApiPokemonCatalog(client, permits, properties);

		assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
			Thread caller = Thread.currentThread();
			Thread interrupter = new Thread(() -> {
				while (caller.getState() != Thread.State.WAITING && caller.getState() != Thread.State.TIMED_WAITING) {
					Thread.onSpinWait();
				}
				caller.interrupt();
			});
			interrupter.start();
			try {
				assertThatThrownBy(() -> blocked.findSummaryById(1))
						.isInstanceOf(CatalogUnavailableException.class)
						.hasCauseInstanceOf(InterruptedException.class);
				assertThat(caller.isInterrupted()).isTrue();
				assertThat(permits.availablePermits()).isZero();
			}
			finally {
				Thread.interrupted();
			}
		});
	}

	private void expectPokemonStatus(HttpStatus status) {
		server.expect(requestTo(BASE + "/pokemon/1")).andRespond(withStatus(status));
	}

	private void assertUnavailableWithRestClientCause() {
		assertThatThrownBy(() -> catalog.findSummaryById(1))
				.isInstanceOf(CatalogUnavailableException.class)
				.hasCauseInstanceOf(RestClientException.class);
	}

	private static org.springframework.test.web.client.ResponseCreator jsonBody(String body) {
		return withSuccess(body, MediaType.APPLICATION_JSON);
	}

}
