package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonPage;
import com.fred.pokehub.domain.model.PokemonReference;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;
import com.fred.pokehub.domain.model.StoredPokemon;
import com.fred.pokehub.support.PokemonSamples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class ListPokemonServiceTest {

	private ExecutorService executor;

	private FakeCatalog catalog;

	private FakeStore store;

	@BeforeEach
	void setUp() {
		executor = Executors.newVirtualThreadPerTaskExecutor();
		catalog = new FakeCatalog();
		store = new FakeStore();
	}

	@AfterEach
	void tearDown() {
		executor.shutdownNow();
		Thread.interrupted();
	}

	@Test
	void shouldReturnCountQueryAndSummariesInReferenceOrder() {
		PageQuery query = PageQuery.of(10, 5);
		PokemonSummary first = PokemonSamples.summary(1);
		PokemonSummary second = PokemonSamples.summary(2);
		catalog.page(1351, PokemonSamples.reference(1), PokemonSamples.reference(2));
		catalog.summary(1, first);
		catalog.summary(2, second);

		PokemonPage page = service().list(query);

		assertThat(page.count()).isEqualTo(1351);
		assertThat(page.query()).isSameAs(query);
		assertThat(page.items()).containsExactly(first, second);
	}

	@Test
	void shouldPreserveOrderWhenALaterItemFinishesFirst() {
		CountDownLatch firstStarted = new CountDownLatch(1);
		CountDownLatch secondFinished = new CountDownLatch(1);
		PokemonSummary first = PokemonSamples.summary(1);
		PokemonSummary second = PokemonSamples.summary(2);
		catalog.page(2, PokemonSamples.reference(1), PokemonSamples.reference(2));
		catalog.loader(1, () -> {
			firstStarted.countDown();
			await(secondFinished);
			return first;
		});
		catalog.loader(2, () -> {
			await(firstStarted);
			secondFinished.countDown();
			return second;
		});

		PokemonPage page = service().list(PageQuery.of(20, 0));

		assertThat(page.items()).containsExactly(first, second);
	}

	@Test
	void shouldLoadSummariesInParallel() {
		CountDownLatch arrived = new CountDownLatch(3);
		catalog.page(3, PokemonSamples.reference(1), PokemonSamples.reference(2), PokemonSamples.reference(3));
		catalog.loader(1, parallel(arrived, 1));
		catalog.loader(2, parallel(arrived, 2));
		catalog.loader(3, parallel(arrived, 3));

		PokemonPage page = service().list(PageQuery.of(20, 0));

		assertThat(page.items()).extracting(PokemonSummary::id).containsExactly(1, 2, 3);
		assertThat(catalog.summaryCalls()).isEqualTo(3);
	}

	@Test
	void shouldNotLoadSummariesWhenThePageIsEmpty() {
		PageQuery query = PageQuery.of(20, 40);
		catalog.page(1351);

		PokemonPage page = service().list(query);

		assertThat(page.count()).isEqualTo(1351);
		assertThat(page.query()).isSameAs(query);
		assertThat(page.items()).isEmpty();
		assertThat(catalog.summaryCalls()).isZero();
		assertThat(store.syncedIdCalls).isZero();
	}

	@Test
	void shouldPropagateListFailureAndLoadNothing() {
		IllegalStateException failure = new IllegalStateException("list failed");
		catalog.failList(failure);
		catalog.page(1351, PokemonSamples.reference(1));

		assertThatThrownBy(() -> service().list(PageQuery.of(20, 0))).isSameAs(failure);
		assertThat(catalog.summaryCalls()).isZero();
	}

	@Test
	void shouldCancelRemainingLoadsWhenOneSummaryFails() {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch interrupted = new CountDownLatch(1);
		IllegalStateException boom = new IllegalStateException("boom");
		catalog.page(2, PokemonSamples.reference(1), PokemonSamples.reference(2));
		catalog.loader(1, () -> {
			started.countDown();
			awaitInterrupt(interrupted);
			return PokemonSamples.summary(1);
		});
		catalog.loader(2, () -> {
			await(started);
			throw boom;
		});

		assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
			assertThatThrownBy(() -> service().list(PageQuery.of(20, 0)))
					.isInstanceOf(CatalogUnavailableException.class)
					.hasCause(boom);
			assertThat(interrupted.await(1, TimeUnit.SECONDS)).isTrue();
		});
	}

	@Test
	void shouldRethrowCatalogUnavailableExceptionFromASummaryLoad() {
		CatalogUnavailableException failure = new CatalogUnavailableException("down", new IOException("reset"));
		catalog.page(1, PokemonSamples.reference(1));
		catalog.failSummary(1, failure);

		assertThatThrownBy(() -> service().list(PageQuery.of(20, 0))).isSameAs(failure);
	}

	@Test
	void shouldWrapUnexpectedSummaryFailures() {
		IllegalStateException cause = new IllegalStateException("boom");
		catalog.page(1, PokemonSamples.reference(1));
		catalog.failSummary(1, cause);

		assertThatThrownBy(() -> service().list(PageQuery.of(20, 0)))
				.isInstanceOf(CatalogUnavailableException.class)
				.hasCause(cause);
	}

	@Test
	void shouldFailAndRestoreTheInterruptFlagWhenTheCallerIsInterrupted() {
		CountDownLatch entered = new CountDownLatch(1);
		catalog.page(1, PokemonSamples.reference(1));
		catalog.loader(1, () -> {
			entered.countDown();
			awaitInterrupt(new CountDownLatch(1));
			return PokemonSamples.summary(1);
		});

		assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
			Thread caller = Thread.currentThread();
			Thread interrupter = new Thread(() -> {
				await(entered);
				caller.interrupt();
			});
			interrupter.start();
			try {
				assertThatThrownBy(() -> service().list(PageQuery.of(20, 0)))
						.isInstanceOf(CatalogUnavailableException.class)
						.hasCauseInstanceOf(InterruptedException.class);
				assertThat(caller.isInterrupted()).isTrue();
			}
			finally {
				Thread.interrupted();
			}
		});
	}

	@Test
	void shouldRejectNullCatalog() {
		assertThatThrownBy(() -> new ListPokemonService(null, executor, store))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("catalog");
	}

	@Test
	void shouldRejectNullExecutor() {
		assertThatThrownBy(() -> new ListPokemonService(catalog, null, store))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("executor");
	}

	@Test
	void shouldRejectNullStore() {
		assertThatThrownBy(() -> new ListPokemonService(catalog, executor, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("store");
	}

	@Test
	void shouldFlagEverySummaryWhenTheWholePageIsSynced() {
		catalog.page(2, PokemonSamples.reference(1), PokemonSamples.reference(2));
		catalog.summary(1, PokemonSamples.summary(1));
		catalog.summary(2, PokemonSamples.summary(2));
		store.syncedIds = Set.of(1, 2);

		PokemonPage page = service().list(PageQuery.of(20, 0));

		assertThat(page.items()).allMatch(PokemonSummary::synced);
		assertThat(store.requestedIds).containsExactly(List.of(1, 2));
	}

	@Test
	void shouldLeaveSummariesUnsyncedWhenNoneAreStored() {
		catalog.page(2, PokemonSamples.reference(1), PokemonSamples.reference(2));
		catalog.summary(1, PokemonSamples.summary(1));
		catalog.summary(2, PokemonSamples.summary(2));

		PokemonPage page = service().list(PageQuery.of(50, 0));

		assertThat(page.items()).noneMatch(PokemonSummary::synced);
		assertThat(store.syncedIdCalls).isEqualTo(1);
	}

	@Test
	void shouldFlagOnlyTheStoredIdsFromASingleLookup() {
		catalog.page(3, PokemonSamples.reference(1), PokemonSamples.reference(2), PokemonSamples.reference(3));
		catalog.summary(1, PokemonSamples.summary(1));
		catalog.summary(2, PokemonSamples.summary(2));
		catalog.summary(3, PokemonSamples.summary(3));
		store.syncedIds = Set.of(2);

		PokemonPage page = service().list(PageQuery.of(20, 0));

		assertThat(page.items()).extracting(PokemonSummary::synced).containsExactly(false, true, false);
		assertThat(store.syncedIdCalls).isEqualTo(1);
		assertThat(store.requestedIds).containsExactly(List.of(1, 2, 3));
	}

	private ListPokemonService service() {
		return new ListPokemonService(catalog, executor, store);
	}

	private static SummaryLoader parallel(CountDownLatch arrived, int id) {
		return () -> {
			arrived.countDown();
			await(arrived);
			return PokemonSamples.summary(id);
		};
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(1, TimeUnit.SECONDS)) {
				throw new IllegalStateException("timed out waiting for concurrent load");
			}
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(exception);
		}
	}

	private static void awaitInterrupt(CountDownLatch interrupted) {
		try {
			new CountDownLatch(1).await();
		}
		catch (InterruptedException exception) {
			interrupted.countDown();
			Thread.currentThread().interrupt();
			throw new IllegalStateException("interrupted", exception);
		}
	}

	@FunctionalInterface
	private interface SummaryLoader {

		PokemonSummary load();

	}

	private static final class FakeCatalog implements PokemonCatalog {

		private final Map<Integer, PokemonSummary> summaries = new HashMap<>();

		private final Map<Integer, RuntimeException> failures = new HashMap<>();

		private final Map<Integer, SummaryLoader> loaders = new HashMap<>();

		private final AtomicInteger summaryCalls = new AtomicInteger();

		private PokemonReferencePage references = new PokemonReferencePage(0, java.util.List.of());

		private RuntimeException listFailure;

		void page(int count, PokemonReference... items) {
			references = new PokemonReferencePage(count, java.util.List.of(items));
		}

		void summary(int id, PokemonSummary summary) {
			summaries.put(id, summary);
		}

		void failList(RuntimeException failure) {
			listFailure = failure;
		}

		void failSummary(int id, RuntimeException failure) {
			failures.put(id, failure);
		}

		void loader(int id, SummaryLoader loader) {
			loaders.put(id, loader);
		}

		int summaryCalls() {
			return summaryCalls.get();
		}

		@Override
		public PokemonReferencePage listReferences(PageQuery query) {
			if (listFailure != null) {
				throw listFailure;
			}
			return references;
		}

		@Override
		public PokemonSummary findSummaryById(int id) {
			summaryCalls.incrementAndGet();
			RuntimeException failure = failures.get(id);
			if (failure != null) {
				throw failure;
			}
			SummaryLoader loader = loaders.get(id);
			if (loader != null) {
				return loader.load();
			}
			return summaries.get(id);
		}

		@Override
		public PokemonDetails findDetailsById(int id) {
			throw new UnsupportedOperationException("findDetailsById");
		}

	}

	private static final class FakeStore implements PokemonStore {

		private Set<Integer> syncedIds = Set.of();

		private final List<List<Integer>> requestedIds = new ArrayList<>();

		private int syncedIdCalls;

		@Override
		public Optional<StoredPokemon> findById(int id) {
			throw new UnsupportedOperationException("findById");
		}

		@Override
		public Set<Integer> findSyncedIds(Collection<Integer> ids) {
			syncedIdCalls++;
			requestedIds.add(List.copyOf(ids));
			return syncedIds;
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
