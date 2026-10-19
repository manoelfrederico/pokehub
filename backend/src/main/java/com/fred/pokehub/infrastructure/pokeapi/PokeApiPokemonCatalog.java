package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonReference;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class PokeApiPokemonCatalog implements PokemonCatalog {

	private static final Logger log = LoggerFactory.getLogger(PokeApiPokemonCatalog.class);

	private final PokeApiHttp http;

	private final PokeApiEvolutionChains evolutionChains;

	public PokeApiPokemonCatalog(
			RestClient client,
			Semaphore permits,
			PokeApiProperties properties,
			PokeApiEvolutionChains evolutionChains
	) {
		this.http = new PokeApiHttp(client, permits, properties);
		this.evolutionChains = Objects.requireNonNull(evolutionChains, "evolutionChains");
	}

	@Override
	@Cacheable(cacheNames = CacheNames.POKEMON_REFERENCES)
	public PokemonReferencePage listReferences(PageQuery query) {
		log.info("Fetching pokemon references limit={} offset={}", query.limit(), query.offset());
		return toPage(http.get(http.list(query), PokeApiResponses.PokemonList.class));
	}

	@Override
	@Cacheable(cacheNames = CacheNames.POKEMON_SUMMARIES)
	public PokemonSummary findSummaryById(int id) {
		log.info("Fetching pokemon summary id={}", id);
		PokeApiResponses.Pokemon pokemon = http.get(http.resource("pokemon", id), PokeApiResponses.Pokemon.class);
		PokeApiResponses.Species species = http.get(http.resource("pokemon-species", speciesId(pokemon)), PokeApiResponses.Species.class);
		return PokemonSummaryMapper.toSummary(pokemon, species);
	}

	@Override
	@Cacheable(cacheNames = CacheNames.POKEMON_DETAILS)
	public PokemonDetails findDetailsById(int id) {
		log.info("Fetching pokemon details id={}", id);
		PokeApiResponses.Pokemon pokemon = http.pokemon(id);
		PokeApiResponses.Species species = http.get(http.resource("pokemon-species", speciesId(pokemon)), PokeApiResponses.Species.class);
		return PokemonDetailsMapper.toDetails(pokemon, species, evolutionChains.load(chainId(species)));
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

	private static int chainId(PokeApiResponses.Species species) {
		String url = species.evolutionChain() == null ? null : species.evolutionChain().url();
		return PokeApiUrls.extractId(url);
	}

}
