package com.fred.pokehub.application.port.in;

import com.fred.pokehub.domain.model.SyncResult;

/**
 * Replicates one Pokémon into the local store. Repeating the call refreshes the snapshot only.
 */
public interface SyncPokemonUseCase {

	SyncResult sync(int id);

}
