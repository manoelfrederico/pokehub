package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.Stat;
import com.fred.pokehub.domain.model.StoredPokemon;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoredPokemonMapperTest {

	@Test
	void shouldRoundTripABranchingPokemonWithExtras() {
		StoredPokemon stored = new StoredPokemon(eevee(), extras());

		PokemonEntity entity = entityFor(stored);

		assertThat(StoredPokemonMapper.toDomain(entity)).isEqualTo(stored);
		assertThat(entity.stats()).extracting(PokemonStatEntity::position).containsExactly((short) 0, (short) 1);
		assertThat(entity.abilities()).extracting(PokemonAbilityEntity::hidden).containsExactly(false, true);
	}

	@Test
	void shouldRoundTripASingleStagePokemonWithoutOptionalFields() {
		StoredPokemon stored = new StoredPokemon(ditto(), PokemonExtras.empty());

		PokemonEntity entity = entityFor(stored);

		assertThat(StoredPokemonMapper.toDomain(entity)).isEqualTo(stored);
		assertThat(entity.imageUrl()).isNull();
		assertThat(entity.spriteUrl()).isNull();
		assertThat(entity.species().category()).isNull();
		assertThat(entity.species().description()).isNull();
		assertThat(entity.localizedName()).isNull();
		assertThat(entity.region()).isNull();
		assertThat(entity.tags()).isEmpty();
	}

	@Test
	void shouldReadStatsByPositionWhenTheCollectionIsShuffled() {
		PokemonEntity entity = entityFor(new StoredPokemon(eevee(), extras()));
		PokemonStatEntity first = entity.stats().getFirst();
		entity.stats().set(0, entity.stats().get(1));
		entity.stats().set(1, first);

		assertThat(StoredPokemonMapper.toDomain(entity).details().stats())
				.containsExactly(new Stat("hp", 55), new Stat("attack", 55));
	}

	@Test
	void shouldTreatAMissingEvolutionChainAsASingleNode() {
		PokemonEntity entity = entityFor(new StoredPokemon(ditto(), PokemonExtras.empty()));
		entity.species().setEvolutionChain(null);

		assertThat(StoredPokemonMapper.toDomain(entity).details().evolutionChain())
				.isEqualTo(new EvolutionNode(132, "ditto", null, List.of()));
	}

	@Test
	void shouldLeaveExtrasUntouchedWhenApplyingASnapshot() {
		PokemonEntity entity = entityFor(new StoredPokemon(eevee(), extras()));
		PokemonDetails renamed = new PokemonDetails(
				133,
				"eevee-refreshed",
				"https://images/133.png",
				"https://sprites/133.png",
				"Evolution Pokémon",
				new Mass(70),
				List.of(new Ability(50, "run-away", false)),
				List.of(new Stat("speed", 55)),
				"It has a new description.",
				eeveeChain(),
				133,
				"eevee");

		StoredPokemonMapper.applySnapshot(entity, renamed, entity.species());

		assertThat(entity.name()).isEqualTo("eevee-refreshed");
		assertThat(entity.weightHg()).isEqualTo(70);
		assertThat(entity.localizedName()).isEqualTo("Eievui");
		assertThat(entity.region()).isEqualTo("Kanto");
		assertThat(entity.tags()).extracting(PokemonTagEntity::tag).containsExactly("starter");
		assertThat(entity.abilities()).hasSize(2);
	}

	@Test
	void shouldTreatANullEvolutionChildListAsEmpty() {
		EvolutionChainDocument document = new EvolutionChainDocument(132, "ditto", null, null);

		assertThat(document.toNode()).isEqualTo(new EvolutionNode(132, "ditto", null, List.of()));
	}

	@Test
	void shouldIdentifyRowsByTheirCompositeKeys() {
		PokemonAbilityId ability = new PokemonAbilityId(1, 65);
		PokemonStatId stat = new PokemonStatId(1, (short) 0);
		PokemonTagId tag = new PokemonTagId(1, "starter");

		assertThat(ability).isEqualTo(ability);
		assertThat(ability).isEqualTo(new PokemonAbilityId(1, 65));
		assertThat(ability).isNotEqualTo(new PokemonAbilityId(1, 34));
		assertThat(ability).isNotEqualTo("1-65");
		assertThat(ability.hashCode()).isEqualTo(new PokemonAbilityId(1, 65).hashCode());
		assertThat(stat).isEqualTo(stat);
		assertThat(stat).isEqualTo(new PokemonStatId(1, (short) 0));
		assertThat(stat).isNotEqualTo(new PokemonStatId(2, (short) 0));
		assertThat(stat).isNotEqualTo("stat");
		assertThat(stat.hashCode()).isEqualTo(new PokemonStatId(1, (short) 0).hashCode());
		assertThat(tag).isEqualTo(tag);
		assertThat(tag).isEqualTo(new PokemonTagId(1, "starter"));
		assertThat(tag).isNotEqualTo(new PokemonTagId(1, "grass"));
		assertThat(tag).isNotEqualTo("tag");
		assertThat(tag.hashCode()).isEqualTo(new PokemonTagId(1, "starter").hashCode());
	}

	private static PokemonEntity entityFor(StoredPokemon stored) {
		SpeciesEntity species = StoredPokemonMapper.newSpecies(stored.details());
		List<AbilityEntity> abilities = new ArrayList<>();
		for (Ability ability : stored.details().abilities()) {
			abilities.add(AbilityEntity.create(ability.id(), ability.name()));
		}
		PokemonEntity pokemon = StoredPokemonMapper.newPokemon(stored.details(), species);
		StoredPokemonMapper.replaceAbilities(pokemon, links(pokemon, stored.details(), abilities));
		StoredPokemonMapper.replaceStats(pokemon, stored.details());
		StoredPokemonMapper.replaceExtras(pokemon, stored.extras());
		return pokemon;
	}

	private static List<PokemonAbilityEntity> links(
			PokemonEntity pokemon,
			PokemonDetails details,
			List<AbilityEntity> abilities
	) {
		List<PokemonAbilityEntity> links = new ArrayList<>();
		for (int index = 0; index < details.abilities().size(); index++) {
			PokemonAbilityEntity link = new PokemonAbilityEntity();
			link.assign(pokemon, abilities.get(index), details.abilities().get(index).hidden());
			links.add(link);
		}
		return links;
	}

	private static PokemonExtras extras() {
		return new PokemonExtras("Eievui", "Kanto", List.of("starter"));
	}

	private static PokemonDetails eevee() {
		return new PokemonDetails(
				133,
				"eevee",
				"https://images/133.png",
				"https://sprites/133.png",
				"Evolution Pokémon",
				new Mass(65),
				List.of(new Ability(50, "run-away", false), new Ability(91, "adaptability", true)),
				List.of(new Stat("hp", 55), new Stat("attack", 55)),
				"A rare Pokémon.",
				eeveeChain(),
				133,
				"eevee");
	}

	private static EvolutionNode eeveeChain() {
		EvolutionNode vaporeon = new EvolutionNode(134, "vaporeon", "https://sprites/134.png", List.of());
		EvolutionNode jolteon = new EvolutionNode(135, "jolteon", "https://sprites/135.png", List.of());
		return new EvolutionNode(133, "eevee", "https://sprites/133.png", List.of(vaporeon, jolteon));
	}

	private static PokemonDetails ditto() {
		return new PokemonDetails(
				132,
				"ditto",
				null,
				null,
				null,
				new Mass(40),
				List.of(),
				List.of(),
				null,
				new EvolutionNode(132, "ditto", null, List.of()),
				132,
				"ditto");
	}

}
