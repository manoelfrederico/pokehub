package com.fred.pokehub.domain.model;

/**
 * Outcome of an idempotent sync. {@code created} is true only the first time the snapshot is stored.
 */
public record SyncResult(boolean created, PokemonView view) {

	public SyncResult {
		if (view == null) {
			throw new IllegalArgumentException("view must not be null");
		}
	}

}
