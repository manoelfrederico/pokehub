package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PokemonStatId implements Serializable {

	@Column(name = "pokemon_id")
	private Integer pokemonId;

	@Column(name = "position")
	private short position;

	public PokemonStatId() {
	}

	PokemonStatId(Integer pokemonId, short position) {
		this.pokemonId = pokemonId;
		this.position = position;
	}

	short position() {
		return position;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PokemonStatId that)) {
			return false;
		}
		return Objects.equals(pokemonId, that.pokemonId) && position == that.position;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pokemonId, position);
	}

}
