package com.fred.pokehub.application.service;

import com.fred.pokehub.application.exception.PokemonNotSyncedException;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasCommand;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasUseCase;
import com.fred.pokehub.application.port.out.PokemonStore;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.PokemonView;

import java.util.Objects;

/**
 * Replaces proprietary fields of a stored Pokémon. The PokéAPI snapshot is never written.
 */
public final class UpdatePokemonExtrasService implements UpdatePokemonExtrasUseCase {

	private final PokemonStore store;

	public UpdatePokemonExtrasService(PokemonStore store) {
		this.store = Objects.requireNonNull(store, "store");
	}

	@Override
	public PokemonView update(UpdatePokemonExtrasCommand command) {
		Objects.requireNonNull(command, "command");
		if (command.id() < 1) {
			throw new InvalidPokemonIdException("id must be positive but was " + command.id());
		}
		PokemonExtras extras = new PokemonExtras(command.localizedName(), command.region(), command.tags());
		return store.updateExtras(command.id(), extras)
				.map(stored -> new PokemonView(stored.details(), stored.extras(), true))
				.orElseThrow(() -> new PokemonNotSyncedException(command.id()));
	}

}
