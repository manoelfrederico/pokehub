package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.PokemonSummary;

import java.math.BigDecimal;
import java.util.List;

public record PokemonSummaryResponse(
		int id,
		String name,
		String sprite,
		String category,
		BigDecimal mass,
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
