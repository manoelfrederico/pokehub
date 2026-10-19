package com.fred.pokehub.application.port.out;

import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.StoredPokemon;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Local replica of synced Pokémon. Snapshot writes never change proprietary extras.
 */
public interface PokemonStore {

	Optional<StoredPokemon> findById(int id);

	/**
	 * @param ids candidate Pokémon ids, possibly empty
	 * @return the subset that is stored locally
	 */
	Set<Integer> findSyncedIds(Collection<Integer> ids);

	/**
	 * Inserts or refreshes the PokéAPI snapshot. Extras already stored are left unchanged.
	 *
	 * @return {@code true} when this call created the row
	 */
	boolean saveSnapshot(PokemonDetails details);

	/**
	 * Replaces proprietary extras for one stored Pokémon.
	 * Snapshot columns, abilities, stats and species stay unchanged.
	 *
	 * @return the updated replica, or empty when {@code id} is not stored
	 */
	Optional<StoredPokemon> updateExtras(int id, PokemonExtras extras);

	/**
	 * @return {@code true} when a row was removed
	 */
	boolean deleteById(int id);

}
