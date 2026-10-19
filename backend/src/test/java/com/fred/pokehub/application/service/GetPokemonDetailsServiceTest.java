package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.exception.PokemonNotFoundException;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;
import com.fred.pokehub.domain.model.PokemonView;
import com.fred.pokehub.domain.model.StoredPokemon;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetPokemonDetailsServiceTest {

	@Test
	void shouldReturnAnUnsyncedView() {
		PokemonDetails details = details();
		FakeCatalog catalog = new FakeCatalog(details);
		FakeStore store = new FakeStore();
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog, store);

		PokemonView view = service.get(1);

		assertThat(view.details()).isEqualTo(details);
		assertThat(view.extras()).isNull();
		assertThat(view.synced()).isFalse();
		assertThat(catalog.requestedId).isEqualTo(1);
		assertThat(store.requestedId).isEqualTo(1);
	}

	@Test
	void shouldReturnTheStoredReplicaWithoutCallingTheCatalog() {
		PokemonDetails details = details();
		PokemonExtras extras = new PokemonExtras("Bulbassauro", "Kanto", List.of("starter"));
		FakeCatalog catalog = new FakeCatalog(details);
		FakeStore store = new FakeStore(new StoredPokemon(details, extras));
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog, store);

		PokemonView view = service.get(1);

		assertThat(view.details()).isEqualTo(details);
		assertThat(view.extras()).isEqualTo(extras);
		assertThat(view.synced()).isTrue();
		assertThat(catalog.requestedId).isNull();
	}

	@Test
	void shouldRejectANonPositiveIdBeforeCallingTheCatalog() {
		FakeCatalog catalog = new FakeCatalog(details());
		FakeStore store = new FakeStore();
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog, store);

		assertThatThrownBy(() -> service.get(0))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> service.get(-1))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("-1");
		assertThat(catalog.requestedId).isNull();
		assertThat(store.requestedId).isNull();
	}

	@Test
	void shouldPropagatePokemonNotFound() {
		PokemonNotFoundException failure = new PokemonNotFoundException(99);
		FakeCatalog catalog = new FakeCatalog(failure);
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog, new FakeStore());

		assertThatThrownBy(() -> service.get(99)).isSameAs(failure);
	}

	@Test
	void shouldPropagateCatalogUnavailable() {
		CatalogUnavailableException failure = new CatalogUnavailableException("down", new IllegalStateException("down"));
		FakeCatalog catalog = new FakeCatalog(failure);
		GetPokemonDetailsService service = new GetPokemonDetailsService(catalog, new FakeStore());

		assertThatThrownBy(() -> service.get(1)).isSameAs(failure);
	}

	@Test
	void shouldRejectANullCatalog() {
		assertThatThrownBy(() -> new GetPokemonDetailsService(null, new FakeStore()))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("catalog");
	}

	@Test
	void shouldRejectANullStore() {
		assertThatThrownBy(() -> new GetPokemonDetailsService(new FakeCatalog(details()), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("store");
	}

	private static PokemonDetails details() {
		return new PokemonDetails(
				1,
				"bulbasaur",
				null,
				null,
				null,
				new Mass(69),
				List.of(),
				List.of(),
				null,
				new EvolutionNode(1, "bulbasaur", null, List.of()),
				1,
				"bulbasaur");
	}

	private static final class FakeCatalog implements PokemonCatalog {

		private final PokemonDetails details;

		private final RuntimeException failure;

		private Integer requestedId;

		private FakeCatalog(PokemonDetails details) {
			this.details = details;
			this.failure = null;
		}

		private FakeCatalog(RuntimeException failure) {
			this.details = null;
			this.failure = failure;
		}

		@Override
		public PokemonReferencePage listReferences(PageQuery query) {
			throw new UnsupportedOperationException("listReferences");
		}

		@Override
		public PokemonSummary findSummaryById(int id) {
			throw new UnsupportedOperationException("findSummaryById");
		}

		@Override
		public PokemonDetails findDetailsById(int id) {
			requestedId = id;
			if (failure != null) {
				throw failure;
			}
			return details;
		}

	}

	private static final class FakeStore implements PokemonStore {

		private final StoredPokemon stored;

		private Integer requestedId;

		private FakeStore() {
			this.stored = null;
		}

		private FakeStore(StoredPokemon stored) {
			this.stored = stored;
		}

		@Override
		public Optional<StoredPokemon> findById(int id) {
			requestedId = id;
			return Optional.ofNullable(stored);
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
			throw new UnsupportedOperationException("deleteById");
		}

	}

}
