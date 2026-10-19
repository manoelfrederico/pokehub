package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.PokemonExtras;

import java.util.List;

public record PokemonExtrasResponse(String localizedName, String region, List<String> tags) {

	public static PokemonExtrasResponse from(PokemonExtras extras) {
		return new PokemonExtrasResponse(extras.localizedName(), extras.region(), extras.tags());
	}

}
