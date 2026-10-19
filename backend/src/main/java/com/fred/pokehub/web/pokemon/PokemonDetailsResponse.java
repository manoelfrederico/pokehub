package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record PokemonDetailsResponse(
		int id,
		String name,
		@Schema(nullable = true) String image,
		@Schema(nullable = true) String sprite,
		@Schema(nullable = true) String category,
		@Schema(description = "Mass in kilograms") BigDecimal mass,
		List<SkillResponse> skills,
		List<StatResponse> stats,
		@Schema(nullable = true) String description,
		EvolutionNodeResponse evolutionChain,
		boolean synced,
		@Schema(description = "Null when synced is false") PokemonExtrasResponse extras
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
