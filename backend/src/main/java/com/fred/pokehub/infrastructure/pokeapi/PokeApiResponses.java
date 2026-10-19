package com.fred.pokehub.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

final class PokeApiResponses {

	private PokeApiResponses() {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record PokemonList(Integer count, List<NamedResource> results) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record NamedResource(String name, String url) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Pokemon(
			int id,
			String name,
			int weight,
			List<AbilitySlot> abilities,
			Sprites sprites,
			NamedResource species
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record AbilitySlot(NamedResource ability, @JsonProperty("is_hidden") boolean hidden) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Sprites(@JsonProperty("front_default") String frontDefault, OtherSprites other) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record OtherSprites(@JsonProperty("official-artwork") Artwork officialArtwork, Artwork home) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Artwork(@JsonProperty("front_default") String frontDefault) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Species(List<Genus> genera) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Genus(String genus, Language language) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Language(String name) {
	}

}
