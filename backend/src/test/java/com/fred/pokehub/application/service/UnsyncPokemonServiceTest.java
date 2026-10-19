package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.PokemonNotSyncedException;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.StoredPokemon;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnsyncPokemonServiceTest {

	@Test
	void shouldDeleteAStoredPokemon() {
		FakeStore store = new FakeStore(true);
		UnsyncPokemonService service = new UnsyncPokemonService(store);

		service.unsync(1);

		assertThat(store.deletedId).isEqualTo(1);
	}

	@Test
	void shouldRejectAMissingPokemon() {
		FakeStore store = new FakeStore(false);
		UnsyncPokemonService service = new UnsyncPokemonService(store);

		assertThatThrownBy(() -> service.unsync(7))
				.isInstanceOf(PokemonNotSyncedException.class)
				.hasMessageContaining("7");
		assertThat(store.deletedId).isEqualTo(7);
	}

	@Test
	void shouldRejectANonPositiveIdBeforeCallingTheStore() {
		FakeStore store = new FakeStore(true);
		UnsyncPokemonService service = new UnsyncPokemonService(store);

		assertThatThrownBy(() -> service.unsync(0))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> service.unsync(-4))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("-4");
		assertThat(store.deletedId).isNull();
	}

	@Test
	void shouldRejectANullStore() {
		assertThatThrownBy(() -> new UnsyncPokemonService(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("store");
	}

	private static final class FakeStore implements PokemonStore {

		private final boolean deleted;

		private Integer deletedId;

		private FakeStore(boolean deleted) {
			this.deleted = deleted;
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
		public boolean deleteById(int id) {
			deletedId = id;
			return deleted;
		}

	}

}
