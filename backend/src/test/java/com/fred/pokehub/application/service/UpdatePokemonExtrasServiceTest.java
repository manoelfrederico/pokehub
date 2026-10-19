package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.PokemonNotSyncedException;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasCommand;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.exception.InvalidPokemonExtrasException;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.PokemonView;
import com.fred.pokehub.domain.model.Stat;
import com.fred.pokehub.domain.model.StoredPokemon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdatePokemonExtrasServiceTest {

	@Test
	void shouldReturnTheUpdatedViewAndLeaveTheSnapshotUnchanged() {
		PokemonDetails snapshot = bulbasaur();
		FakeStore store = new FakeStore(new StoredPokemon(snapshot, PokemonExtras.empty()));
		UpdatePokemonExtrasService service = new UpdatePokemonExtrasService(store);
		UpdatePokemonExtrasCommand command = new UpdatePokemonExtrasCommand(
				1, "  Bulbassauro  ", "\tKanto\n", List.of("  GRASS ", "Fire"));

		PokemonView view = service.update(command);

		assertThat(view.synced()).isTrue();
		assertThat(view.details()).isEqualTo(snapshot);
		assertThat(view.extras()).isEqualTo(new PokemonExtras("Bulbassauro", "Kanto", List.of("grass", "fire")));
		assertThat(store.stored.details()).isEqualTo(snapshot);
		assertThat(store.stored.extras()).isEqualTo(view.extras());
		assertThat(store.updateCalls).isEqualTo(1);
	}

	@Test
	void shouldRejectAPokemonThatIsNotStored() {
		FakeStore store = new FakeStore(null);
		UpdatePokemonExtrasService service = new UpdatePokemonExtrasService(store);

		assertThatThrownBy(() -> service.update(new UpdatePokemonExtrasCommand(7, "Nome", "Kanto", List.of())))
				.isInstanceOf(PokemonNotSyncedException.class)
				.hasMessageContaining("7");
		assertThat(store.saved).isFalse();
		assertThat(store.stored).isNull();
	}

	@Test
	void shouldRejectInvalidExtrasBeforeCallingTheStore() {
		FakeStore store = new FakeStore(new StoredPokemon(bulbasaur(), PokemonExtras.empty()));
		UpdatePokemonExtrasService service = new UpdatePokemonExtrasService(store);

		assertThatThrownBy(() -> service.update(new UpdatePokemonExtrasCommand(1, "n".repeat(101), null, List.of("A", "a"))))
				.isInstanceOf(InvalidPokemonExtrasException.class);
		assertThat(store.updateCalls).isZero();
	}

	@Test
	void shouldKeepTheSameExtrasWhenTheCommandIsRepeated() {
		FakeStore store = new FakeStore(new StoredPokemon(bulbasaur(), PokemonExtras.empty()));
		UpdatePokemonExtrasService service = new UpdatePokemonExtrasService(store);
		UpdatePokemonExtrasCommand command = new UpdatePokemonExtrasCommand(1, "Bulbassauro", "Kanto", List.of("starter"));

		PokemonView first = service.update(command);
		PokemonView second = service.update(command);

		assertThat(second).isEqualTo(first);
		assertThat(store.stored.extras()).isEqualTo(new PokemonExtras("Bulbassauro", "Kanto", List.of("starter")));
		assertThat(store.updateCalls).isEqualTo(2);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -1})
	void shouldRejectANonPositiveIdBeforeCallingTheStore(int id) {
		FakeStore store = new FakeStore(new StoredPokemon(bulbasaur(), PokemonExtras.empty()));
		UpdatePokemonExtrasService service = new UpdatePokemonExtrasService(store);

		assertThatThrownBy(() -> service.update(new UpdatePokemonExtrasCommand(id, "Nome", "Kanto", null)))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining(String.valueOf(id));
		assertThat(store.updateCalls).isZero();
	}

	@Test
	void shouldPropagateAStoreFailure() {
		FakeStore store = new FakeStore(new StoredPokemon(bulbasaur(), PokemonExtras.empty()));
		store.updateFailure = new IllegalStateException("disk");
		UpdatePokemonExtrasService service = new UpdatePokemonExtrasService(store);

		assertThatThrownBy(() -> service.update(new UpdatePokemonExtrasCommand(1, "Nome", null, List.of())))
				.isSameAs(store.updateFailure);
	}

	@Test
	void shouldRejectANullCommand() {
		UpdatePokemonExtrasService service = new UpdatePokemonExtrasService(new FakeStore(null));

		assertThatThrownBy(() -> service.update(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("command");
	}

	@Test
	void shouldRejectANullStore() {
		assertThatThrownBy(() -> new UpdatePokemonExtrasService(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("store");
	}

	private static PokemonDetails bulbasaur() {
		return new PokemonDetails(
				1,
				"bulbasaur",
				"https://images/1.png",
				"https://sprites/1.png",
				"Seed Pokémon",
				new Mass(69),
				List.of(new Ability(65, "overgrow", false)),
				List.of(new Stat("hp", 45)),
				"A strange seed.",
				new EvolutionNode(1, "bulbasaur", "https://sprites/1.png", List.of()),
				1,
				"bulbasaur");
	}

	private static final class FakeStore implements PokemonStore {

		private StoredPokemon stored;

		private RuntimeException updateFailure;

		private int updateCalls;

		private boolean saved;

		private FakeStore(StoredPokemon stored) {
			this.stored = stored;
		}

		@Override
		public Optional<StoredPokemon> findById(int id) {
			throw new UnsupportedOperationException("findById");
		}

		@Override
		public Set<Integer> findSyncedIds(Collection<Integer> ids) {
			throw new UnsupportedOperationException("findSyncedIds");
		}

		@Override
		public boolean saveSnapshot(PokemonDetails details) {
			throw new UnsupportedOperationException("saveSnapshot");
		}

		@Override
		public Optional<StoredPokemon> updateExtras(int id, PokemonExtras extras) {
			updateCalls++;
			if (updateFailure != null) {
				throw updateFailure;
			}
			if (stored == null || stored.details().id() != id) {
				return Optional.empty();
			}
			stored = new StoredPokemon(stored.details(), extras);
			saved = true;
			return Optional.of(stored);
		}

		@Override
		public boolean deleteById(int id) {
			throw new UnsupportedOperationException("deleteById");
		}

	}

}
