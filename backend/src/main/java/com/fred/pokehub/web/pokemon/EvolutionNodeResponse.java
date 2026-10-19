package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.EvolutionNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record EvolutionNodeResponse(
		int id,
		String name,
		@Schema(nullable = true) String sprite,
		List<EvolutionNodeResponse> evolvesTo) {

	public static EvolutionNodeResponse from(EvolutionNode node) {
		return new EvolutionNodeResponse(
				node.id(),
				node.name(),
				node.spriteUrl(),
				node.evolvesTo().stream().map(EvolutionNodeResponse::from).toList());
	}

}
