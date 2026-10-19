package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonReference;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class PokeApiPokemonCatalog implements PokemonCatalog {

	private static final Logger log = LoggerFactory.getLogger(PokeApiPokemonCatalog.class);

	private static final String UNAVAILABLE = "The Pokémon catalog is temporarily unavailable.";

	private final RestClient client;

	private final Semaphore permits;

	private final PokeApiProperties properties;

	public PokeApiPokemonCatalog(RestClient client, Semaphore permits, PokeApiProperties properties) {
		this.client = Objects.requireNonNull(client, "client");
		this.permits = Objects.requireNonNull(permits, "permits");
		this.properties = Objects.requireNonNull(properties, "properties");
	}

	@Override
	@Cacheable(cacheNames = CacheNames.POKEMON_REFERENCES)
	public PokemonReferencePage listReferences(PageQuery query) {
		log.info("Fetching pokemon references limit={} offset={}", query.limit(), query.offset());
		return toPage(get(listUri(query), PokeApiResponses.PokemonList.class));
	}

	@Override
	@Cacheable(cacheNames = CacheNames.POKEMON_SUMMARIES)
	public PokemonSummary findSummaryById(int id) {
		log.info("Fetching pokemon summary id={}", id);
		PokeApiResponses.Pokemon pokemon = get(resourceUri("pokemon", id), PokeApiResponses.Pokemon.class);
		PokeApiResponses.Species species = get(resourceUri("pokemon-species", speciesId(pokemon)), PokeApiResponses.Species.class);
		return PokemonSummaryMapper.toSummary(pokemon, species);
	}

	private PokemonReferencePage toPage(PokeApiResponses.PokemonList list) {
		if (list.count() == null) {
			throw new CatalogUnavailableException("Pokemon list is missing count", new IllegalStateException("count"));
		}
		return new PokemonReferencePage(list.count(), references(list.results()));
	}

	private static List<PokemonReference> references(List<PokeApiResponses.NamedResource> results) {
		if (results == null) {
			return List.of();
		}
		List<PokemonReference> references = new ArrayList<>();
		for (PokeApiResponses.NamedResource result : results) {
			references.add(new PokemonReference(PokeApiUrls.extractId(result.url()), result.name()));
		}
		return List.copyOf(references);
	}

	private static int speciesId(PokeApiResponses.Pokemon pokemon) {
		String url = pokemon.species() == null ? null : pokemon.species().url();
		return PokeApiUrls.extractId(url);
	}

	private <T> T get(URI uri, Class<T> bodyType) {
		acquire();
		try {
			return requireBody(client.get().uri(uri).retrieve().body(bodyType));
		}
		catch (RestClientException exception) {
			throw new CatalogUnavailableException(UNAVAILABLE, exception);
		}
		finally {
			permits.release();
		}
	}

	private static <T> T requireBody(T body) {
		if (body == null) {
			throw new CatalogUnavailableException(
					"Pokemon catalog returned an empty body",
					new IllegalStateException("empty body"));
		}
		return body;
	}

	private void acquire() {
		try {
			permits.acquire();
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new CatalogUnavailableException(UNAVAILABLE, exception);
		}
	}

	private URI listUri(PageQuery query) {
		return URI.create(baseUrl() + "/pokemon?limit=" + query.limit() + "&offset=" + query.offset());
	}

	private URI resourceUri(String resource, int id) {
		return URI.create(baseUrl() + "/" + resource + "/" + id);
	}

	private String baseUrl() {
		String baseUrl = properties.baseUrl();
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

}
