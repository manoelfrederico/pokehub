package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.domain.model.EvolutionNode;

import java.util.ArrayList;
import java.util.List;

final class EvolutionChainMapper {

	static final String SPRITE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png";

	private EvolutionChainMapper() {
	}

	static EvolutionNode toNode(PokeApiResponses.ChainLink link) {
		if (link == null || link.species() == null) {
			throw unavailable("Evolution chain link is missing a species");
		}
		PokeApiResponses.NamedResource species = link.species();
		if (species.name() == null || species.name().isBlank()) {
			throw unavailable("Evolution species name is blank");
		}
		int id = PokeApiUrls.extractId(species.url());
		return new EvolutionNode(id, species.name(), SPRITE_URL.formatted(id), children(link.evolvesTo()));
	}

	private static List<EvolutionNode> children(List<PokeApiResponses.ChainLink> evolvesTo) {
		if (evolvesTo == null) {
			return List.of();
		}
		List<EvolutionNode> children = new ArrayList<>();
		for (PokeApiResponses.ChainLink child : evolvesTo) {
			children.add(toNode(child));
		}
		return List.copyOf(children);
	}

	private static CatalogUnavailableException unavailable(String message) {
		return new CatalogUnavailableException(message, new IllegalArgumentException(message));
	}

}
