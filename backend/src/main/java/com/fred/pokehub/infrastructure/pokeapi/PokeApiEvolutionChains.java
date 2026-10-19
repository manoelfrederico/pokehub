package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.domain.model.EvolutionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Semaphore;

class PokeApiEvolutionChains {

	private static final Logger log = LoggerFactory.getLogger(PokeApiEvolutionChains.class);

	private final PokeApiHttp http;

	PokeApiEvolutionChains(RestClient client, Semaphore permits, PokeApiProperties properties) {
		this.http = new PokeApiHttp(client, permits, properties);
	}

	@Cacheable(cacheNames = CacheNames.EVOLUTION_CHAINS)
	public EvolutionNode load(int chainId) {
		log.info("Fetching evolution chain id={}", chainId);
		PokeApiResponses.EvolutionChain body = http.get(
				http.resource("evolution-chain", chainId),
				PokeApiResponses.EvolutionChain.class);
		if (body.chain() == null) {
			throw new CatalogUnavailableException("Evolution chain is missing", new IllegalStateException("chain"));
		}
		return EvolutionChainMapper.toNode(body.chain());
	}

}
