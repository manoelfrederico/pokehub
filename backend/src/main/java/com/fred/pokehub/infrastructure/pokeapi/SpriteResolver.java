package com.fred.pokehub.infrastructure.pokeapi;

import java.util.Optional;

final class SpriteResolver {

	private SpriteResolver() {
	}

	static Optional<String> listingSprite(PokeApiResponses.Sprites sprites) {
		if (sprites == null) {
			return Optional.empty();
		}
		Optional<String> frontDefault = nonBlank(sprites.frontDefault());
		if (frontDefault.isPresent()) {
			return frontDefault;
		}
		return fallback(sprites.other());
	}

	private static Optional<String> fallback(PokeApiResponses.OtherSprites other) {
		if (other == null) {
			return Optional.empty();
		}
		Optional<String> artwork = frontDefault(other.officialArtwork());
		if (artwork.isPresent()) {
			return artwork;
		}
		return frontDefault(other.home());
	}

	private static Optional<String> frontDefault(PokeApiResponses.Artwork artwork) {
		if (artwork == null) {
			return Optional.empty();
		}
		return nonBlank(artwork.frontDefault());
	}

	private static Optional<String> nonBlank(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(value);
	}

}
