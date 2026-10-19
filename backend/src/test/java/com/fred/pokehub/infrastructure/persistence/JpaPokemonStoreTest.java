package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaPokemonStoreTest {

	@Mock
	private SpeciesRepository species;

	@Mock
	private PokemonRepository pokemon;

	@Mock
	private AbilityRepository abilities;

	private JpaPokemonStore store;

	@BeforeEach
	void setUp() {
		store = new JpaPokemonStore(species, pokemon, abilities);
		lenient().when(species.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		lenient().when(abilities.save(any(AbilityEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		lenient().when(pokemon.save(any(PokemonEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void shouldCreateSpeciesPokemonAbilitiesAndStats() {
		when(species.findById(1)).thenReturn(Optional.empty());
		when(abilities.findById(65)).thenReturn(Optional.empty());
		when(pokemon.findById(1)).thenReturn(Optional.empty());

		boolean created = store.saveSnapshot(bulbasaur(new Ability(65, "overgrow", true)));

		assertThat(created).isTrue();
		verify(species).save(any(SpeciesEntity.class));
		verify(abilities).save(any(AbilityEntity.class));
		PokemonEntity saved = savedPokemon();
		assertThat(saved.id()).isEqualTo(1);
		assertThat(saved.name()).isEqualTo("bulbasaur");
		assertThat(saved.weightHg()).isEqualTo(69);
		assertThat(saved.species().id()).isEqualTo(1);
		assertThat(saved.species().name()).isEqualTo("bulbasaur");
		assertThat(saved.abilities()).extracting(link -> link.ability().id()).containsExactly(65);
		assertThat(saved.abilities()).extracting(PokemonAbilityEntity::hidden).containsExactly(true);
		assertThat(saved.stats()).extracting(PokemonStatEntity::name).containsExactly("hp", "attack");
		assertThat(saved.stats()).extracting(PokemonStatEntity::position).containsExactly((short) 0, (short) 1);
		assertThat(saved.localizedName()).isNull();
		assertThat(saved.region()).isNull();
		assertThat(saved.tags()).isEmpty();
	}

	@Test
	void shouldRefreshTheSnapshotAndLeaveExtrasUntouched() {
		PokemonEntity existing = storedBulbasaur();
		when(species.findById(1)).thenReturn(Optional.of(existing.species()));
		when(abilities.findById(34)).thenReturn(Optional.empty());
		when(pokemon.findById(1)).thenReturn(Optional.of(existing));
		PokemonDetails refreshed = new PokemonDetails(
				1,
				"bulbasaur-refreshed",
				"https://images/1b.png",
				"https://sprites/1b.png",
				"Seed Pokémon",
				new Mass(70),
				List.of(new Ability(34, "chlorophyll", false)),
				List.of(new Stat("speed", 45)),
				"Updated.",
				new EvolutionNode(1, "bulbasaur", null, List.of()),
				1,
				"bulbasaur");

		boolean created = store.saveSnapshot(refreshed);

		assertThat(created).isFalse();
		verify(species, never()).save(any());
		PokemonEntity saved = savedPokemon();
		assertThat(saved).isSameAs(existing);
		assertThat(saved.name()).isEqualTo("bulbasaur-refreshed");
		assertThat(saved.imageUrl()).isEqualTo("https://images/1b.png");
		assertThat(saved.spriteUrl()).isEqualTo("https://sprites/1b.png");
		assertThat(saved.weightHg()).isEqualTo(70);
		assertThat(saved.species().description()).isEqualTo("Updated.");
		assertThat(saved.abilities()).extracting(link -> link.ability().name()).containsExactly("chlorophyll");
		assertThat(saved.stats()).extracting(PokemonStatEntity::name).containsExactly("speed");
		assertThat(saved.localizedName()).isEqualTo("Bulbassauro");
		assertThat(saved.region()).isEqualTo("Kanto");
		assertThat(saved.tags()).extracting(PokemonTagEntity::tag).containsExactly("starter");
	}

	@Test
	void shouldReuseAnAbilityRowInsteadOfSavingItAgain() {
		AbilityEntity overgrow = AbilityEntity.create(65, "overgrow");
		when(abilities.findById(65)).thenReturn(Optional.of(overgrow));
		when(species.findById(1)).thenReturn(Optional.empty());
		when(pokemon.findById(1)).thenReturn(Optional.empty());
		when(pokemon.findById(2)).thenReturn(Optional.empty());

		store.saveSnapshot(bulbasaur(new Ability(65, "overgrow", false)));
		store.saveSnapshot(ivysaur(new Ability(65, "overgrow", true)));

		verify(abilities, never()).save(any());
		ArgumentCaptor<PokemonEntity> captor = ArgumentCaptor.forClass(PokemonEntity.class);
		verify(pokemon, times(2)).save(captor.capture());
		assertThat(captor.getAllValues())
				.allSatisfy(saved -> assertThat(saved.abilities().getFirst().ability()).isSameAs(overgrow));
	}

	@Test
	void shouldReuseASpeciesRowAcrossPokemon() {
		SpeciesEntity shared = StoredPokemonMapper.newSpecies(bulbasaur(new Ability(65, "overgrow", false)));
		when(species.findById(1)).thenReturn(Optional.empty(), Optional.of(shared));
		when(species.save(any(SpeciesEntity.class))).thenReturn(shared);
		when(abilities.findById(65)).thenReturn(Optional.of(AbilityEntity.create(65, "overgrow")));
		when(pokemon.findById(1)).thenReturn(Optional.empty());
		when(pokemon.findById(2)).thenReturn(Optional.empty());

		store.saveSnapshot(bulbasaur(new Ability(65, "overgrow", false)));
		store.saveSnapshot(ivysaur(new Ability(65, "overgrow", false)));

		verify(species, times(1)).save(any());
		ArgumentCaptor<PokemonEntity> captor = ArgumentCaptor.forClass(PokemonEntity.class);
		verify(pokemon, times(2)).save(captor.capture());
		assertThat(captor.getValue().species()).isSameAs(shared);
	}

	@Test
	void shouldDelegateFindSyncedIdsOnce() {
		when(pokemon.findIdsByIdIn(List.of(1, 2, 3))).thenReturn(Set.of(2));

		assertThat(store.findSyncedIds(List.of(1, 2, 3))).containsExactly(2);
		verify(pokemon, times(1)).findIdsByIdIn(List.of(1, 2, 3));
	}

	@Test
	void shouldNotQueryWhenTheIdCollectionIsEmpty() {
		assertThat(store.findSyncedIds(List.of())).isEmpty();

		verifyNoInteractions(pokemon);
	}

	@Test
	void shouldRejectNullIds() {
		assertThatThrownBy(() -> store.findSyncedIds(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("ids");
	}

	@Test
	void shouldReturnFalseWhenDeletingAMissingPokemon() {
		when(pokemon.existsById(9)).thenReturn(false);

		assertThat(store.deleteById(9)).isFalse();

		verify(pokemon, never()).deleteById(any());
		verify(species, never()).delete(any());
		verify(abilities, never()).delete(any());
	}

	@Test
	void shouldDeleteOnlyThePokemonRow() {
		when(pokemon.existsById(1)).thenReturn(true);

		assertThat(store.deleteById(1)).isTrue();

		verify(pokemon).deleteById(1);
		verify(species, never()).delete(any());
		verify(abilities, never()).delete(any());
	}

	@Test
	void shouldReturnEmptyWhenThePokemonIsNotStored() {
		when(pokemon.findById(4)).thenReturn(Optional.empty());

		assertThat(store.findById(4)).isEmpty();
	}

	@Test
	void shouldMapAStoredPokemon() {
		PokemonEntity existing = storedBulbasaur();
		when(pokemon.findById(1)).thenReturn(Optional.of(existing));

		assertThat(store.findById(1)).contains(StoredPokemonMapper.toDomain(existing));
	}

	private PokemonEntity savedPokemon() {
		ArgumentCaptor<PokemonEntity> captor = ArgumentCaptor.forClass(PokemonEntity.class);
		verify(pokemon, times(1)).save(captor.capture());
		return captor.getValue();
	}

	private static PokemonEntity storedBulbasaur() {
		PokemonDetails details = bulbasaur(new Ability(65, "overgrow", false));
		SpeciesEntity species = StoredPokemonMapper.newSpecies(details);
		PokemonEntity pokemon = StoredPokemonMapper.newPokemon(details, species);
		PokemonAbilityEntity link = new PokemonAbilityEntity();
		link.assign(pokemon, AbilityEntity.create(65, "overgrow"), false);
		StoredPokemonMapper.replaceAbilities(pokemon, List.of(link));
		StoredPokemonMapper.replaceStats(pokemon, details);
		StoredPokemonMapper.replaceExtras(pokemon, new com.fred.pokehub.domain.model.PokemonExtras("Bulbassauro", "Kanto", List.of("starter")));
		return pokemon;
	}

	private static PokemonDetails bulbasaur(Ability ability) {
		return details(1, "bulbasaur", 1, "bulbasaur", ability);
	}

	private static PokemonDetails ivysaur(Ability ability) {
		return details(2, "ivysaur", 1, "bulbasaur", ability);
	}

	private static PokemonDetails details(int id, String name, int speciesId, String speciesName, Ability ability) {
		return new PokemonDetails(
				id,
				name,
				null,
				null,
				"Seed Pokémon",
				new Mass(69),
				List.of(ability),
				List.of(new Stat("hp", 45), new Stat("attack", 49)),
				"A strange seed.",
				new EvolutionNode(1, "bulbasaur", null, List.of()),
				speciesId,
				speciesName);
	}

}
