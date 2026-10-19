package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonSummary;
import com.fred.pokehub.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringJUnitConfig(PokeApiPokemonCatalogCacheTest.CacheSlice.class)
class PokeApiPokemonCatalogCacheTest {

	private static final String BASE = "http://catalog.test/api/v2";

	@Autowired
	private PokemonCatalog catalog;

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void resetServerAndCaches() {
		server.reset();
		for (String name : cacheManager.getCacheNames()) {
			cacheManager.getCache(name).clear();
		}
	}

	@Test
	void shouldNotCallTheServerAgainForAnEqualPageQuery() {
		server.expect(once(), requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andRespond(json(Fixtures.read("pokeapi/list-first-page.json")));

		catalog.listReferences(PageQuery.of(2, 0));
		catalog.listReferences(new PageQuery(2, 0));

		server.verify();
	}

	@Test
	void shouldCallTheServerAgainForADifferentPageQuery() {
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=0"))
				.andRespond(json(Fixtures.read("pokeapi/list-first-page.json")));
		server.expect(requestTo(BASE + "/pokemon?limit=2&offset=20"))
				.andRespond(json(Fixtures.read("pokeapi/list-first-page.json")));

		catalog.listReferences(PageQuery.of(2, 0));
		catalog.listReferences(PageQuery.of(2, 20));

		server.verify();
	}

	@Test
	void shouldNotCallTheServerAgainForTheSameSummaryId() {
		expectBulbasaur();

		PokemonSummary first = catalog.findSummaryById(1);
		PokemonSummary second = catalog.findSummaryById(1);

		assertThat(second).isEqualTo(first);
		server.verify();
	}

	@Test
	void shouldNotCacheAFailedSummaryLookup() {
		server.expect(requestTo(BASE + "/pokemon/1")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
		assertThatThrownBy(() -> catalog.findSummaryById(1)).isInstanceOf(CatalogUnavailableException.class);

		server.reset();
		expectBulbasaur();
		assertThat(catalog.findSummaryById(1).name()).isEqualTo("bulbasaur");
		server.verify();
	}

	private void expectBulbasaur() {
		server.expect(once(), requestTo(BASE + "/pokemon/1"))
				.andRespond(json(Fixtures.read("pokeapi/pokemon-1.json")));
		server.expect(once(), requestTo(BASE + "/pokemon-species/1"))
				.andRespond(json(Fixtures.read("pokeapi/species-1.json")));
	}

	private static org.springframework.test.web.client.ResponseCreator json(String body) {
		return withSuccess(body, MediaType.APPLICATION_JSON);
	}

	@Configuration
	@EnableCaching(proxyTargetClass = true)
	static class CacheSlice {

		@Bean
		PokeApiProperties pokeApiProperties() {
			return new PokeApiProperties(BASE, Duration.ofSeconds(1), Duration.ofSeconds(1), 4);
		}

		@Bean
		RestClient.Builder pokeApiRestClientBuilder(PokeApiProperties properties) {
			return PokeApiClientConfig.restClientBuilder(properties);
		}

		@Bean
		MockRestServiceServer mockRestServiceServer(RestClient.Builder pokeApiRestClientBuilder) {
			return MockRestServiceServer.bindTo(pokeApiRestClientBuilder).build();
		}

		@Bean
		RestClient pokeApiRestClient(RestClient.Builder pokeApiRestClientBuilder, MockRestServiceServer server) {
			return pokeApiRestClientBuilder.build();
		}

		@Bean
		PokemonCatalog pokemonCatalog(RestClient pokeApiRestClient, PokeApiProperties properties) {
			return new PokeApiPokemonCatalog(pokeApiRestClient, new Semaphore(4), properties);
		}

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CacheNames.POKEMON_REFERENCES, CacheNames.POKEMON_SUMMARIES);
		}

	}

}
