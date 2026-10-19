package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.StoredPokemon;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
public class JpaPokemonStore implements PokemonStore {

	private final SpeciesRepository species;

	private final PokemonRepository pokemon;

	private final AbilityRepository abilities;

	public JpaPokemonStore(SpeciesRepository species, PokemonRepository pokemon, AbilityRepository abilities) {
		this.species = Objects.requireNonNull(species, "species");
		this.pokemon = Objects.requireNonNull(pokemon, "pokemon");
		this.abilities = Objects.requireNonNull(abilities, "abilities");
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoredPokemon> findById(int id) {
		return pokemon.findById(id).map(StoredPokemonMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Set<Integer> findSyncedIds(Collection<Integer> ids) {
		Objects.requireNonNull(ids, "ids");
		if (ids.isEmpty()) {
			return Set.of();
		}
		return Set.copyOf(pokemon.findIdsByIdIn(ids));
	}

	@Override
	@Transactional
	public boolean saveSnapshot(PokemonDetails details) {
		Objects.requireNonNull(details, "details");
		SpeciesEntity speciesRow = upsertSpecies(details);
		return pokemon.findById(details.id())
				.map(existing -> refresh(existing, details, speciesRow))
				.orElseGet(() -> insert(details, speciesRow));
	}

	@Override
	@Transactional
	public boolean deleteById(int id) {
		if (!pokemon.existsById(id)) {
			return false;
		}
		pokemon.deleteById(id);
		return true;
	}

	private SpeciesEntity upsertSpecies(PokemonDetails details) {
		return species.findById(details.speciesId())
				.map(existing -> {
					StoredPokemonMapper.applySpecies(existing, details);
					return existing;
				})
				.orElseGet(() -> species.save(StoredPokemonMapper.newSpecies(details)));
	}

	private boolean insert(PokemonDetails details, SpeciesEntity speciesRow) {
		PokemonEntity created = StoredPokemonMapper.newPokemon(details, speciesRow);
		StoredPokemonMapper.replaceAbilities(created, links(created, details));
		StoredPokemonMapper.replaceStats(created, details);
		pokemon.save(created);
		return true;
	}

	private boolean refresh(PokemonEntity existing, PokemonDetails details, SpeciesEntity speciesRow) {
		StoredPokemonMapper.applySnapshot(existing, details, speciesRow);
		StoredPokemonMapper.replaceAbilities(existing, links(existing, details));
		StoredPokemonMapper.replaceStats(existing, details);
		pokemon.save(existing);
		return false;
	}

	private List<PokemonAbilityEntity> links(PokemonEntity pokemonRow, PokemonDetails details) {
		List<PokemonAbilityEntity> links = new ArrayList<>();
		for (Ability ability : details.abilities()) {
			PokemonAbilityEntity link = new PokemonAbilityEntity();
			link.assign(pokemonRow, abilityRow(ability), ability.hidden());
			links.add(link);
		}
		return links;
	}

	private AbilityEntity abilityRow(Ability ability) {
		return abilities.findById(ability.id())
				.map(existing -> rename(existing, ability.name()))
				.orElseGet(() -> abilities.save(AbilityEntity.create(ability.id(), ability.name())));
	}

	private static AbilityEntity rename(AbilityEntity existing, String name) {
		existing.setName(name);
		return existing;
	}

}
