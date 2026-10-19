package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.PokemonSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record PokemonSummaryResponse(
		int id,
		String name,
		@Schema(nullable = true) String sprite,
		@Schema(nullable = true) String category,
		@Schema(description = "Mass in kilograms") BigDecimal mass,
		List<SkillResponse> skills,
		boolean synced
) {

	public static PokemonSummaryResponse from(PokemonSummary summary) {
		return new PokemonSummaryResponse(
				summary.id(),
				summary.name(),
				summary.spriteUrl(),
				summary.category(),
				summary.mass().kilograms(),
				summary.abilities().stream().map(SkillResponse::from).toList(),
				summary.synced());
	}

}
