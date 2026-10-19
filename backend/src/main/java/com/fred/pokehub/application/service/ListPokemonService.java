package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.out.PokemonCatalog;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonPage;
import com.fred.pokehub.domain.model.PokemonReference;
import com.fred.pokehub.domain.model.PokemonReferencePage;
import com.fred.pokehub.domain.model.PokemonSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Loads a page of references, then each summary concurrently, failing the whole page on the first error.
 * One store lookup flags which ids on the page are already replicated.
 */
public final class ListPokemonService implements ListPokemonUseCase {

	static final String UNAVAILABLE = "The Pokémon catalog is temporarily unavailable.";

	private final PokemonCatalog catalog;

	private final ExecutorService executor;

	private final PokemonStore store;

	public ListPokemonService(PokemonCatalog catalog, ExecutorService executor, PokemonStore store) {
		this.catalog = Objects.requireNonNull(catalog, "catalog");
		this.executor = Objects.requireNonNull(executor, "executor");
		this.store = Objects.requireNonNull(store, "store");
	}

	@Override
	public PokemonPage list(PageQuery query) {
		PokemonReferencePage references = catalog.listReferences(query);
		if (references.items().isEmpty()) {
			return new PokemonPage(references.count(), query, List.of());
		}
		List<PokemonSummary> summaries = loadSummaries(references.items());
		return new PokemonPage(references.count(), query, flagSynced(summaries));
	}

	private List<PokemonSummary> flagSynced(List<PokemonSummary> summaries) {
		List<Integer> ids = summaries.stream().map(PokemonSummary::id).toList();
		Set<Integer> synced = store.findSyncedIds(ids);
		return summaries.stream()
				.map(summary -> summary.withSynced(synced.contains(summary.id())))
				.toList();
	}

	private List<PokemonSummary> loadSummaries(List<PokemonReference> references) {
		ExecutorCompletionService<IndexedSummary> completion = new ExecutorCompletionService<>(executor);
		List<Future<IndexedSummary>> futures = submitAll(completion, references);
		try {
			return awaitAll(completion, references.size());
		}
		catch (InterruptedException exception) {
			cancelAll(futures);
			Thread.currentThread().interrupt();
			throw new CatalogUnavailableException(UNAVAILABLE, exception);
		}
		catch (CatalogUnavailableException exception) {
			cancelAll(futures);
			throw exception;
		}
	}

	private List<Future<IndexedSummary>> submitAll(
			ExecutorCompletionService<IndexedSummary> completion,
			List<PokemonReference> references
	) {
		List<Future<IndexedSummary>> futures = new ArrayList<>();
		for (int index = 0; index < references.size(); index++) {
			futures.add(completion.submit(task(index, references.get(index).id())));
		}
		return List.copyOf(futures);
	}

	private Callable<IndexedSummary> task(int index, int id) {
		return () -> new IndexedSummary(index, catalog.findSummaryById(id));
	}

	private static List<PokemonSummary> awaitAll(ExecutorCompletionService<IndexedSummary> completion, int size)
			throws InterruptedException {
		PokemonSummary[] summaries = new PokemonSummary[size];
		for (int remaining = size; remaining > 0; remaining--) {
			IndexedSummary indexed = next(completion);
			summaries[indexed.index()] = indexed.summary();
		}
		return List.of(summaries);
	}

	private static IndexedSummary next(ExecutorCompletionService<IndexedSummary> completion) throws InterruptedException {
		try {
			return completion.take().get();
		}
		catch (ExecutionException exception) {
			throw translate(exception.getCause());
		}
	}

	private static CatalogUnavailableException translate(Throwable cause) {
		if (cause instanceof CatalogUnavailableException catalogUnavailable) {
			return catalogUnavailable;
		}
		if (cause instanceof RuntimeException runtimeException) {
			return new CatalogUnavailableException(UNAVAILABLE, runtimeException);
		}
		return new CatalogUnavailableException(UNAVAILABLE, cause);
	}

	private static void cancelAll(List<Future<IndexedSummary>> futures) {
		for (Future<IndexedSummary> future : futures) {
			future.cancel(true);
		}
	}

	private record IndexedSummary(int index, PokemonSummary summary) {
	}

}
