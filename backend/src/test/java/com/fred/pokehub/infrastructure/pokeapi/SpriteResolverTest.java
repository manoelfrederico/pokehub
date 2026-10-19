package com.fred.pokehub.infrastructure.pokeapi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpriteResolverTest {

	@Test
	void shouldPreferFrontDefault() {
		PokeApiResponses.Sprites sprites = new PokeApiResponses.Sprites(
				"https://sprites/front.png",
				new PokeApiResponses.OtherSprites(artwork("https://sprites/art.png"), artwork("https://sprites/home.png")));

		assertThat(SpriteResolver.listingSprite(sprites)).contains("https://sprites/front.png");
	}

	@Test
	void shouldFallBackToOfficialArtworkWhenFrontDefaultIsBlank() {
		PokeApiResponses.Sprites sprites = new PokeApiResponses.Sprites(
				"  ",
				new PokeApiResponses.OtherSprites(artwork("https://sprites/art.png"), artwork("https://sprites/home.png")));

		assertThat(SpriteResolver.listingSprite(sprites)).contains("https://sprites/art.png");
	}

	@Test
	void shouldFallBackToHomeWhenArtworkIsBlank() {
		PokeApiResponses.Sprites sprites = new PokeApiResponses.Sprites(
				null,
				new PokeApiResponses.OtherSprites(artwork(" "), artwork("https://sprites/home.png")));

		assertThat(SpriteResolver.listingSprite(sprites)).contains("https://sprites/home.png");
	}

	@Test
	void shouldSkipNullSpritesOtherAndNestedArtwork() {
		assertThat(SpriteResolver.listingSprite(null)).isEmpty();
		assertThat(SpriteResolver.listingSprite(new PokeApiResponses.Sprites(null, null))).isEmpty();
		assertThat(SpriteResolver.listingSprite(new PokeApiResponses.Sprites(null, new PokeApiResponses.OtherSprites(null, null))))
				.isEmpty();
		assertThat(SpriteResolver.listingSprite(
				new PokeApiResponses.Sprites(null, new PokeApiResponses.OtherSprites(null, artwork(null)))))
				.isEmpty();
	}

	@Test
	void shouldBeEmptyWhenNoSpriteIsAvailable() {
		PokeApiResponses.Sprites sprites = new PokeApiResponses.Sprites(
				"",
				new PokeApiResponses.OtherSprites(artwork(""), artwork("   ")));

		assertThat(SpriteResolver.listingSprite(sprites)).isEmpty();
	}

	private static PokeApiResponses.Artwork artwork(String frontDefault) {
		return new PokeApiResponses.Artwork(frontDefault);
	}

}
