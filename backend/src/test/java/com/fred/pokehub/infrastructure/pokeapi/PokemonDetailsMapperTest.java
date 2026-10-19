package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.Stat;
import com.fred.pokehub.support.Fixtures;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonDetailsMapperTest {

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private static final String BULBASAUR_ARTWORK =
			"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png";

	private static final String BULBASAUR_SPRITE =
			"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png";

	private static final String BULBASAUR_DESCRIPTION =
			"A strange seed was planted on its back at birth. The plant sprouts and grows with this POKéMON.";

	@Test
	void shouldMapBulbasaurFromFixtures() {
		PokemonDetails details = PokemonDetailsMapper.toDetails(
				pokemon("pokeapi/pokemon-1.json"),
				species("pokeapi/species-1.json"),
				EvolutionChainMapper.toNode(chain("pokeapi/evolution-chain-1.json")));

		assertThat(details.id()).isEqualTo(1);
		assertThat(details.name()).isEqualTo("bulbasaur");
		assertThat(details.imageUrl()).isEqualTo(BULBASAUR_ARTWORK);
		assertThat(details.spriteUrl()).isEqualTo(BULBASAUR_SPRITE);
		assertThat(details.category()).isEqualTo("Seed Pokémon");
		assertThat(details.mass()).isEqualTo(new Mass(69));
		assertThat(details.abilities()).containsExactly(
				new Ability("overgrow", false),
				new Ability("chlorophyll", true));
		assertThat(details.stats()).containsExactly(new Stat("hp", 45));
		assertThat(details.description()).isEqualTo(BULBASAUR_DESCRIPTION);
		assertThat(details.evolutionChain().name()).isEqualTo("bulbasaur");
		assertThat(details.evolutionChain().evolvesTo().getFirst().evolvesTo().getFirst().name()).isEqualTo("venusaur");
	}

	@Test
	void shouldKeepThePokemonIdWhenTheSpeciesIdDiffers() {
		PokemonDetails details = PokemonDetailsMapper.toDetails(
				pokemon("pokeapi/pokemon-10033.json"),
				species("pokeapi/species-3.json"),
				new EvolutionNode(1, "bulbasaur", null, List.of()));

		assertThat(details.id()).isEqualTo(10033);
		assertThat(details.name()).isEqualTo("venusaur-mega");
		assertThat(details.category()).isEqualTo("Seed Pokémon");
		assertThat(pokemon("pokeapi/pokemon-10033.json").species().url()).endsWith("/pokemon-species/3/");
	}

	@Test
	void shouldKeepStatOrderAndNames() {
		PokeApiResponses.Pokemon pokemon = new PokeApiResponses.Pokemon(
				1,
				"bulbasaur",
				69,
				List.of(),
				List.of(
						stat("hp", 45),
						stat("attack", 49),
						stat("defense", 49),
						stat("special-attack", 65),
						stat("special-defense", 65),
						stat("speed", 45)),
				null,
				null);

		assertThat(PokemonDetailsMapper.toDetails(pokemon, null, new EvolutionNode(1, "bulbasaur", null, List.of())).stats())
				.containsExactly(
						new Stat("hp", 45),
						new Stat("attack", 49),
						new Stat("defense", 49),
						new Stat("special-attack", 65),
						new Stat("special-defense", 65),
						new Stat("speed", 45));
	}

	@Test
	void shouldSkipStatsWithoutANameAndTreatNullStatsAsEmpty() {
		PokeApiResponses.Pokemon pokemon = new PokeApiResponses.Pokemon(
				1,
				"bulbasaur",
				69,
				null,
				Arrays.asList(
						null,
						new PokeApiResponses.StatSlot(10, null),
						new PokeApiResponses.StatSlot(10, new PokeApiResponses.NamedResource("  ", null)),
						stat("hp", 45)),
				null,
				null);

		PokemonDetails details = PokemonDetailsMapper.toDetails(pokemon, null, new EvolutionNode(1, "bulbasaur", null, List.of()));

		assertThat(details.stats()).containsExactly(new Stat("hp", 45));
		assertThat(details.abilities()).isEmpty();
		assertThat(details.category()).isNull();
		assertThat(details.description()).isNull();
		assertThat(details.imageUrl()).isNull();
		assertThat(details.spriteUrl()).isNull();
	}

	@Test
	void shouldPreferArtworkThenFrontDefaultThenHome() {
		assertThat(image(sprites("https://sprites/art.png", "https://sprites/front.png", "https://sprites/home.png")))
				.isEqualTo("https://sprites/art.png");
		assertThat(image(sprites("  ", "https://sprites/front.png", "https://sprites/home.png")))
				.isEqualTo("https://sprites/front.png");
		assertThat(image(sprites(null, null, "https://sprites/home.png")))
				.isEqualTo("https://sprites/home.png");
		assertThat(image(sprites(null, " ", null))).isNull();
		assertThat(image(null)).isNull();
		assertThat(image(new PokeApiResponses.Sprites("https://sprites/front.png", null))).isEqualTo("https://sprites/front.png");
		assertThat(image(new PokeApiResponses.Sprites(null, null))).isNull();
	}

	private static String image(PokeApiResponses.Sprites sprites) {
		return PokemonDetailsMapper.toDetails(pokemonWith(sprites), null, new EvolutionNode(1, "bulbasaur", null, List.of()))
				.imageUrl();
	}

	private static PokeApiResponses.Pokemon pokemonWith(PokeApiResponses.Sprites sprites) {
		return new PokeApiResponses.Pokemon(1, "bulbasaur", 69, List.of(), List.of(), sprites, null);
	}

	private static PokeApiResponses.Sprites sprites(String artwork, String frontDefault, String home) {
		return new PokeApiResponses.Sprites(frontDefault, new PokeApiResponses.OtherSprites(artwork(artwork), artwork(home)));
	}

	private static PokeApiResponses.Artwork artwork(String frontDefault) {
		return new PokeApiResponses.Artwork(frontDefault);
	}

	private static PokeApiResponses.StatSlot stat(String name, int baseStat) {
		return new PokeApiResponses.StatSlot(baseStat, new PokeApiResponses.NamedResource(name, "https://pokeapi.co/api/v2/stat/1/"));
	}

	private static PokeApiResponses.Pokemon pokemon(String resource) {
		return JSON.readValue(Fixtures.read(resource), PokeApiResponses.Pokemon.class);
	}

	private static PokeApiResponses.Species species(String resource) {
		return JSON.readValue(Fixtures.read(resource), PokeApiResponses.Species.class);
	}

	private static PokeApiResponses.ChainLink chain(String resource) {
		return JSON.readValue(Fixtures.read(resource), PokeApiResponses.EvolutionChain.class).chain();
	}

}
