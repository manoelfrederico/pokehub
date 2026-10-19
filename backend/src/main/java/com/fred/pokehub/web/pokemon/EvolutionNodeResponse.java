package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.EvolutionNode;

import java.util.List;

public record EvolutionNodeResponse(int id, String name, String sprite, List<EvolutionNodeResponse> evolvesTo) {

	public static EvolutionNodeResponse from(EvolutionNode node) {
		return new EvolutionNodeResponse(
				node.id(),
				node.name(),
				node.spriteUrl(),
				node.evolvesTo().stream().map(EvolutionNodeResponse::from).toList());
	}

}
