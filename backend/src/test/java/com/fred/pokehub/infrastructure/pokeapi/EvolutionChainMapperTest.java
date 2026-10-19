package com.fred.pokehub.infrastructure.pokeapi;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.support.Fixtures;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvolutionChainMapperTest {

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private static final String SPRITE = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png";

	@Test
	void shouldMapTheBulbasaurLineageFromTheFixture() {
		EvolutionNode root = EvolutionChainMapper.toNode(chain("pokeapi/evolution-chain-1.json"));

		assertThat(root.id()).isEqualTo(1);
		assertThat(root.name()).isEqualTo("bulbasaur");
		assertThat(root.spriteUrl()).isEqualTo(SPRITE.formatted(1));
		assertThat(root.evolvesTo()).hasSize(1);

		EvolutionNode ivysaur = root.evolvesTo().getFirst();
		assertThat(ivysaur.id()).isEqualTo(2);
		assertThat(ivysaur.name()).isEqualTo("ivysaur");
		assertThat(ivysaur.spriteUrl()).isEqualTo(SPRITE.formatted(2));

		EvolutionNode venusaur = ivysaur.evolvesTo().getFirst();
		assertThat(venusaur.id()).isEqualTo(3);
		assertThat(venusaur.name()).isEqualTo("venusaur");
		assertThat(venusaur.spriteUrl()).isEqualTo(SPRITE.formatted(3));
		assertThat(venusaur.evolvesTo()).isEmpty();
	}

	@Test
	void shouldMapABranchingLineage() {
		EvolutionNode eevee = EvolutionChainMapper.toNode(chain("pokeapi/evolution-chain-67.json"));

		assertThat(eevee.id()).isEqualTo(133);
		assertThat(eevee.name()).isEqualTo("eevee");
		assertThat(eevee.evolvesTo()).extracting(EvolutionNode::name)
				.containsExactly("vaporeon", "jolteon", "flareon", "espeon", "umbreon", "leafeon", "glaceon", "sylveon");
		assertThat(eevee.evolvesTo()).extracting(EvolutionNode::id)
				.containsExactly(134, 135, 136, 196, 197, 470, 471, 700);
		assertThat(eevee.evolvesTo()).allSatisfy(child -> assertThat(child.evolvesTo()).isEmpty());
	}

	@Test
	void shouldMapASingleStagePokemon() {
		EvolutionNode ditto = EvolutionChainMapper.toNode(link("ditto", "https://pokeapi.co/api/v2/pokemon-species/132/", List.of()));

		assertThat(ditto.id()).isEqualTo(132);
		assertThat(ditto.name()).isEqualTo("ditto");
		assertThat(ditto.evolvesTo()).isEmpty();
	}

	@Test
	void shouldTreatANullEvolvesToAsNoChildren() {
		EvolutionNode node = EvolutionChainMapper.toNode(link("ditto", "https://pokeapi.co/api/v2/pokemon-species/132/", null));

		assertThat(node.evolvesTo()).isEmpty();
	}

	@Test
	void shouldReadTheNodeIdFromTheSpeciesUrl() {
		EvolutionNode node = EvolutionChainMapper.toNode(
				link("venusaur", "https://pokeapi.co/api/v2/pokemon-species/3/", List.of()));

		assertThat(node.id()).isEqualTo(3);
	}

	@Test
	void shouldFailWhenTheSpeciesUrlIsMalformed() {
		assertThatThrownBy(() -> EvolutionChainMapper.toNode(link("broken", "https://pokeapi.co/api/v2/pokemon-species/nope", List.of())))
				.isInstanceOf(CatalogUnavailableException.class)
				.cause()
				.isNotNull();
	}

	@Test
	void shouldFailWhenTheLinkHasNoSpecies() {
		assertThatThrownBy(() -> EvolutionChainMapper.toNode(null))
				.isInstanceOf(CatalogUnavailableException.class);
		assertThatThrownBy(() -> EvolutionChainMapper.toNode(new PokeApiResponses.ChainLink(null, List.of())))
				.isInstanceOf(CatalogUnavailableException.class);
	}

	@Test
	void shouldFailWhenTheSpeciesNameIsBlank() {
		assertThatThrownBy(() -> EvolutionChainMapper.toNode(link("  ", "https://pokeapi.co/api/v2/pokemon-species/1/", List.of())))
				.isInstanceOf(CatalogUnavailableException.class);
	}

	@Test
	void shouldFailWhenAChildLinkIsNull() {
		assertThatThrownBy(() -> EvolutionChainMapper.toNode(link("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/",
				Arrays.asList(new PokeApiResponses.ChainLink[] { null }))))
				.isInstanceOf(CatalogUnavailableException.class);
	}

	private static PokeApiResponses.ChainLink chain(String resource) {
		return JSON.readValue(Fixtures.read(resource), PokeApiResponses.EvolutionChain.class).chain();
	}

	private static PokeApiResponses.ChainLink link(String name, String url, List<PokeApiResponses.ChainLink> evolvesTo) {
		return new PokeApiResponses.ChainLink(new PokeApiResponses.NamedResource(name, url), evolvesTo);
	}

}
