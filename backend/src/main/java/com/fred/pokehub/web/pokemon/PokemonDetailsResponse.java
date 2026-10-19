package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonView;

import java.math.BigDecimal;
import java.util.List;

public record PokemonDetailsResponse(
		int id,
		String name,
		String image,
		String sprite,
		String category,
		BigDecimal mass,
		List<SkillResponse> skills,
		List<StatResponse> stats,
		String description,
		EvolutionNodeResponse evolutionChain,
		boolean synced,
		PokemonExtrasResponse extras
) {

	public static PokemonDetailsResponse from(PokemonView view) {
		PokemonDetails details = view.details();
		return new PokemonDetailsResponse(
				details.id(),
				details.name(),
				details.imageUrl(),
				details.spriteUrl(),
				details.category(),
				details.mass().kilograms(),
				details.abilities().stream().map(SkillResponse::from).toList(),
				details.stats().stream().map(StatResponse::from).toList(),
				details.description(),
				EvolutionNodeResponse.from(details.evolutionChain()),
				view.synced(),
				view.extras() == null ? null : PokemonExtrasResponse.from(view.extras()));
	}

}
