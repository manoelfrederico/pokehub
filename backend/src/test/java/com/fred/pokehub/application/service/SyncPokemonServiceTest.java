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
import com.fred.pokehub.domain.model.StoredPokemon;
import com.fred.pokehub.domain.model.SyncResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncPokemonServiceTest {

	@Test
	void shouldSaveANewSnapshotAndReportCreated() {
		PokemonDetails details = details();
		List<String> events = new ArrayList<>();
		FakeCatalog catalog = new FakeCatalog(details, events);
		FakeStore store = new FakeStore(events);
		SyncPokemonService service = new SyncPokemonService(catalog, store);

		SyncResult result = service.sync(1);

		assertThat(result.created()).isTrue();
		assertThat(result.view().details()).isEqualTo(details);
		assertThat(result.view().extras()).isEqualTo(PokemonExtras.empty());
		assertThat(result.view().synced()).isTrue();
		assertThat(catalog.requestedIds).containsExactly(1);
		assertThat(store.saved).containsExactly(details);
		assertThat(events).containsExactly("catalog", "save", "find");
	}

	@Test
	void shouldRefreshAnExistingSnapshotAndKeepExtras() {
		PokemonExtras extras = new PokemonExtras("Bulbassauro", "Kanto", List.of("starter"));
		PokemonDetails details = details();
		FakeStore store = new FakeStore(new ArrayList<>());
		store.hold(new StoredPokemon(details, extras));
		SyncPokemonService service = new SyncPokemonService(new FakeCatalog(details, new ArrayList<>()), store);

		SyncResult result = service.sync(1);

		assertThat(result.created()).isFalse();
		assertThat(result.view().extras()).isEqualTo(extras);
		assertThat(result.view().details()).isEqualTo(details);
		assertThat(result.view().synced()).isTrue();
		assertThat(store.saveCount).isEqualTo(1);
	}

	@Test
	void shouldPropagateNotFoundAndSaveNothing() {
		PokemonNotFoundException failure = new PokemonNotFoundException(99);
		FakeStore store = new FakeStore(new ArrayList<>());
		SyncPokemonService service = new SyncPokemonService(new FakeCatalog(failure, new ArrayList<>()), store);

		assertThatThrownBy(() -> service.sync(99)).isSameAs(failure);
		assertThat(store.saveCount).isZero();
	}

	@Test
	void shouldPropagateCatalogUnavailableAndSaveNothing() {
		CatalogUnavailableException failure = new CatalogUnavailableException("down", new IllegalStateException("down"));
		FakeStore store = new FakeStore(new ArrayList<>());
		SyncPokemonService service = new SyncPokemonService(new FakeCatalog(failure, new ArrayList<>()), store);

		assertThatThrownBy(() -> service.sync(1)).isSameAs(failure);
		assertThat(store.saveCount).isZero();
	}

	@Test
	void shouldPropagateStoreFailure() {
		IllegalStateException failure = new IllegalStateException("db down");
		FakeCatalog catalog = new FakeCatalog(details(), new ArrayList<>());
		FakeStore store = new FakeStore(new ArrayList<>());
		store.saveFailure = failure;
		SyncPokemonService service = new SyncPokemonService(catalog, store);

		assertThatThrownBy(() -> service.sync(1)).isSameAs(failure);
		assertThat(catalog.requestedIds).containsExactly(1);
		assertThat(store.findCount).isZero();
	}

	@Test
	void shouldFailWhenTheSnapshotCannotBeReadBack() {
		FakeStore store = new FakeStore(new ArrayList<>());
		store.dropAfterSave = true;
		SyncPokemonService service = new SyncPokemonService(new FakeCatalog(details(), new ArrayList<>()), store);

		assertThatThrownBy(() -> service.sync(1))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("1");
	}

	@Test
	void shouldRejectANonPositiveIdBeforeCallingPorts() {
		FakeCatalog catalog = new FakeCatalog(details(), new ArrayList<>());
		FakeStore store = new FakeStore(new ArrayList<>());
		SyncPokemonService service = new SyncPokemonService(catalog, store);

		assertThatThrownBy(() -> service.sync(0))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> service.sync(-1))
				.isInstanceOf(InvalidPokemonIdException.class)
				.hasMessageContaining("-1");
		assertThat(catalog.requestedIds).isEmpty();
		assertThat(store.saveCount).isZero();
	}

	@Test
	void shouldRejectNullDependencies() {
		FakeCatalog catalog = new FakeCatalog(details(), new ArrayList<>());
		FakeStore store = new FakeStore(new ArrayList<>());

		assertThatThrownBy(() -> new SyncPokemonService(null, store))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("catalog");
		assertThatThrownBy(() -> new SyncPokemonService(catalog, null))
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

		private final List<Integer> requestedIds = new ArrayList<>();

		private final List<String> events;

		private FakeCatalog(PokemonDetails details, List<String> events) {
			this.details = details;
			this.failure = null;
			this.events = events;
		}

		private FakeCatalog(RuntimeException failure, List<String> events) {
			this.details = null;
			this.failure = failure;
			this.events = events;
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
			events.add("catalog");
			requestedIds.add(id);
			if (failure != null) {
				throw failure;
			}
			return details;
		}

	}

	private static final class FakeStore implements PokemonStore {

		private final List<PokemonDetails> saved = new ArrayList<>();

		private final List<String> events;

		private StoredPokemon stored;

		private RuntimeException saveFailure;

		private boolean dropAfterSave;

		private int saveCount;

		private int findCount;

		private FakeStore(List<String> events) {
			this.events = events;
		}

		private void hold(StoredPokemon stored) {
			this.stored = stored;
		}

		@Override
		public Optional<StoredPokemon> findById(int id) {
			findCount++;
			events.add("find");
			return Optional.ofNullable(stored);
		}

		@Override
		public Set<Integer> findSyncedIds(Collection<Integer> ids) {
			throw new UnsupportedOperationException("findSyncedIds");
		}

		@Override
		public boolean saveSnapshot(PokemonDetails details) {
			saveCount++;
			events.add("save");
			saved.add(details);
			if (saveFailure != null) {
				throw saveFailure;
			}
			boolean created = stored == null;
			PokemonExtras extras = stored == null ? PokemonExtras.empty() : stored.extras();
			stored = dropAfterSave ? null : new StoredPokemon(details, extras);
			return created;
		}

		@Override
		public boolean deleteById(int id) {
			throw new UnsupportedOperationException("deleteById");
		}

	}

}
