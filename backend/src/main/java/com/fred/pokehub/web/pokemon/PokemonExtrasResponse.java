package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.PokemonExtras;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PokemonExtrasResponse(
		@Schema(nullable = true, maxLength = 100) String localizedName,
		@Schema(nullable = true, maxLength = 100) String region,
		List<String> tags) {

	public static PokemonExtrasResponse from(PokemonExtras extras) {
		return new PokemonExtrasResponse(extras.localizedName(), extras.region(), extras.tags());
	}

}
