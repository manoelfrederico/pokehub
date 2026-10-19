package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.domain.model.PokemonPage;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

public record PokemonPageResponse(int count, String next, String previous, List<PokemonSummaryResponse> results) {

	public static PokemonPageResponse from(PokemonPage page) {
		int limit = page.query().limit();
		return new PokemonPageResponse(
				page.count(),
				link(limit, page.nextOffset()),
				link(limit, page.previousOffset()),
				page.items().stream().map(PokemonSummaryResponse::from).toList());
	}

	private static String link(int limit, Optional<Integer> offset) {
		return offset.map(value -> ServletUriComponentsBuilder.fromCurrentRequestUri()
				.replaceQuery(null)
				.queryParam("limit", limit)
				.queryParam("offset", value)
				.build()
				.toUriString()).orElse(null);
	}

}
