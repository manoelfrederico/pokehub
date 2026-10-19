package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.exception.PokemonNotFoundException;
import com.fred.pokehub.domain.model.PageQuery;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

final class PokeApiHttp {

	static final String UNAVAILABLE = "The Pokémon catalog is temporarily unavailable.";

	private final RestClient client;

	private final Semaphore permits;

	private final PokeApiProperties properties;

	PokeApiHttp(RestClient client, Semaphore permits, PokeApiProperties properties) {
		this.client = Objects.requireNonNull(client, "client");
		this.permits = Objects.requireNonNull(permits, "permits");
		this.properties = Objects.requireNonNull(properties, "properties");
	}

	<T> T get(URI uri, Class<T> bodyType) {
		return exchange(uri, bodyType, this::unavailable);
	}

	PokeApiResponses.Pokemon pokemon(int id) {
		return exchange(resource("pokemon", id), PokeApiResponses.Pokemon.class, exception -> translatePokemonFailure(id, exception));
	}

	URI list(PageQuery query) {
		return URI.create(baseUrl() + "/pokemon?limit=" + query.limit() + "&offset=" + query.offset());
	}

	URI resource(String resource, int id) {
		return URI.create(baseUrl() + "/" + resource + "/" + id);
	}

	private <T> T exchange(URI uri, Class<T> bodyType, Function<RestClientException, RuntimeException> onError) {
		acquire();
		try {
			return requireBody(client.get().uri(uri).retrieve().body(bodyType));
		}
		catch (RestClientException exception) {
			throw onError.apply(exception);
		}
		finally {
			permits.release();
		}
	}

	private RuntimeException translatePokemonFailure(int id, RestClientException exception) {
		if (exception instanceof RestClientResponseException response
				&& response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
			return new PokemonNotFoundException(id);
		}
		return unavailable(exception);
	}

	private CatalogUnavailableException unavailable(RestClientException exception) {
		return new CatalogUnavailableException(UNAVAILABLE, exception);
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

	private String baseUrl() {
		String baseUrl = properties.baseUrl();
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

}
