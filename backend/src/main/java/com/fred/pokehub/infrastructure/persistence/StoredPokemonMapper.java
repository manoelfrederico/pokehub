package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.Stat;
import com.fred.pokehub.domain.model.StoredPokemon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class StoredPokemonMapper {

	private StoredPokemonMapper() {
	}

	static StoredPokemon toDomain(PokemonEntity pokemon) {
		return new StoredPokemon(details(pokemon), extras(pokemon));
	}

	static SpeciesEntity newSpecies(PokemonDetails details) {
		SpeciesEntity species = new SpeciesEntity();
		species.setId(details.speciesId());
		applySpecies(species, details);
		return species;
	}

	static void applySpecies(SpeciesEntity species, PokemonDetails details) {
		species.setName(details.speciesName());
		species.setCategory(details.category());
		species.setDescription(details.description());
		species.setEvolutionChain(EvolutionChainDocument.from(details.evolutionChain()));
	}

	static PokemonEntity newPokemon(PokemonDetails details, SpeciesEntity species) {
		PokemonEntity pokemon = new PokemonEntity();
		pokemon.setId(details.id());
		applySnapshot(pokemon, details, species);
		return pokemon;
	}

	static void applySnapshot(PokemonEntity pokemon, PokemonDetails details, SpeciesEntity species) {
		pokemon.setName(details.name());
		pokemon.setSpriteUrl(details.spriteUrl());
		pokemon.setImageUrl(details.imageUrl());
		pokemon.setWeightHg(details.mass().hectograms());
		pokemon.setSpecies(species);
	}

	static void replaceAbilities(PokemonEntity pokemon, List<PokemonAbilityEntity> links) {
		pokemon.abilities().clear();
		pokemon.abilities().addAll(links);
	}

	static void replaceStats(PokemonEntity pokemon, PokemonDetails details) {
		pokemon.stats().clear();
		List<Stat> stats = details.stats();
		for (int index = 0; index < stats.size(); index++) {
			Stat stat = stats.get(index);
			PokemonStatEntity row = new PokemonStatEntity();
			row.assign(pokemon, (short) index, stat.name(), stat.baseValue());
			pokemon.stats().add(row);
		}
	}

	static void replaceExtras(PokemonEntity pokemon, PokemonExtras extras) {
		pokemon.setLocalizedName(extras.localizedName());
		pokemon.setRegion(extras.region());
		pokemon.tags().clear();
		for (String tag : extras.tags()) {
			PokemonTagEntity row = new PokemonTagEntity();
			row.assign(pokemon, tag);
			pokemon.tags().add(row);
		}
	}

	private static PokemonDetails details(PokemonEntity pokemon) {
		SpeciesEntity species = pokemon.species();
		return new PokemonDetails(
				pokemon.id(),
				pokemon.name(),
				pokemon.imageUrl(),
				pokemon.spriteUrl(),
				species.category(),
				new Mass(pokemon.weightHg()),
				abilities(pokemon),
				stats(pokemon),
				species.description(),
				chain(pokemon, species),
				species.id(),
				species.name());
	}

	private static EvolutionNode chain(PokemonEntity pokemon, SpeciesEntity species) {
		if (species.evolutionChain() == null) {
			return new EvolutionNode(pokemon.id(), pokemon.name(), pokemon.spriteUrl(), List.of());
		}
		return species.evolutionChain().toNode();
	}

	private static List<Ability> abilities(PokemonEntity pokemon) {
		List<Ability> abilities = new ArrayList<>();
		for (PokemonAbilityEntity link : pokemon.abilities()) {
			abilities.add(new Ability(link.ability().id(), link.ability().name(), link.hidden()));
		}
		return List.copyOf(abilities);
	}

	private static List<Stat> stats(PokemonEntity pokemon) {
		return pokemon.stats().stream()
				.sorted(Comparator.comparingInt(PokemonStatEntity::position))
				.map(stat -> new Stat(stat.name(), stat.baseValue()))
				.toList();
	}

	private static PokemonExtras extras(PokemonEntity pokemon) {
		List<String> tags = new ArrayList<>();
		for (PokemonTagEntity tag : pokemon.tags()) {
			tags.add(tag.tag());
		}
		return new PokemonExtras(pokemon.localizedName(), pokemon.region(), tags);
	}

}
