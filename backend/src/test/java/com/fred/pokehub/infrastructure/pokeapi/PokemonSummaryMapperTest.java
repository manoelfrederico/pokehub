package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonSummary;
import com.fred.pokehub.support.Fixtures;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonSummaryMapperTest {

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private static final String BULBASAUR_SPRITE =
			"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png";

	private static final String MEGA_ARTWORK =
			"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/10033.png";

	@Test
	void shouldMapBulbasaurFromFixtures() {
		PokemonSummary summary = PokemonSummaryMapper.toSummary(pokemon("pokeapi/pokemon-1.json"), species("pokeapi/species-1.json"));

		assertThat(summary.id()).isEqualTo(1);
		assertThat(summary.name()).isEqualTo("bulbasaur");
		assertThat(summary.spriteUrl()).isEqualTo(BULBASAUR_SPRITE);
		assertThat(summary.category()).isEqualTo("Seed Pokémon");
		assertThat(summary.mass()).isEqualTo(new Mass(69));
		assertThat(summary.abilities()).containsExactly(
				new Ability("overgrow", false),
				new Ability("chlorophyll", true));
	}

	@Test
	void shouldSelectTheEnglishGenusAmongMany() {
		PokeApiResponses.Species species = new PokeApiResponses.Species(List.of(
				genus("たねポケモン", "ja-hrkt"),
				genus("Pokémon Graine", "fr"),
				genus("Seed Pokémon", "en"),
				genus("Samen-Pokémon", "de")));

		assertThat(PokemonSummaryMapper.toSummary(pokemon("pokeapi/pokemon-1.json"), species).category())
				.isEqualTo("Seed Pokémon");
	}

	@Test
	void shouldLeaveCategoryNullWhenNoEnglishGenusExists() {
		PokeApiResponses.Species species = new PokeApiResponses.Species(List.of(
				genus("たねポケモン", "ja-hrkt"),
				genus("Pokémon Graine", "fr")));

		assertThat(PokemonSummaryMapper.toSummary(pokemon("pokeapi/pokemon-1.json"), species).category()).isNull();
	}

	@Test
	void shouldLeaveCategoryNullWhenGeneraAreNull() {
		assertThat(PokemonSummaryMapper.toSummary(pokemon("pokeapi/pokemon-1.json"), new PokeApiResponses.Species(null)).category())
				.isNull();
	}

	@Test
	void shouldMapNullAbilitiesToAnEmptyList() {
		PokeApiResponses.Pokemon pokemon = new PokeApiResponses.Pokemon(1, "bulbasaur", 69, null, null, null);

		assertThat(PokemonSummaryMapper.toSummary(pokemon, new PokeApiResponses.Species(null)).abilities()).isEmpty();
	}

	@Test
	void shouldSkipAbilitySlotsWhoseAbilityIsNull() {
		PokeApiResponses.Pokemon pokemon = new PokeApiResponses.Pokemon(
				1,
				"bulbasaur",
				69,
				List.of(
						new PokeApiResponses.AbilitySlot(null, false),
						new PokeApiResponses.AbilitySlot(new PokeApiResponses.NamedResource("overgrow", "https://ability/65"), false)),
				null,
				null);

		assertThat(PokemonSummaryMapper.toSummary(pokemon, new PokeApiResponses.Species(null)).abilities())
				.containsExactly(new Ability("overgrow", false));
	}

	@Test
	void shouldUseOfficialArtworkForAnAlternativeFormWithoutFrontDefault() {
		PokeApiResponses.Pokemon pokemon = pokemon("pokeapi/pokemon-10033.json");

		PokemonSummary summary = PokemonSummaryMapper.toSummary(pokemon, species("pokeapi/species-1.json"));

		assertThat(pokemon.sprites().frontDefault()).isNull();
		assertThat(summary.id()).isEqualTo(10033);
		assertThat(summary.name()).isEqualTo("venusaur-mega");
		assertThat(summary.spriteUrl()).isEqualTo(MEGA_ARTWORK);
		assertThat(summary.mass()).isEqualTo(new Mass(1555));
		assertThat(summary.abilities()).containsExactly(new Ability("thick-fat", false));
	}

	private static PokeApiResponses.Pokemon pokemon(String resource) {
		return JSON.readValue(Fixtures.read(resource), PokeApiResponses.Pokemon.class);
	}

	private static PokeApiResponses.Species species(String resource) {
		return JSON.readValue(Fixtures.read(resource), PokeApiResponses.Species.class);
	}

	private static PokeApiResponses.Genus genus(String name, String language) {
		return new PokeApiResponses.Genus(name, new PokeApiResponses.Language(language));
	}

}
