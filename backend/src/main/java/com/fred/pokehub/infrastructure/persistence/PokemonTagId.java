package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PokemonTagId implements Serializable {

	@Column(name = "pokemon_id")
	private Integer pokemonId;

	@Column(name = "tag", length = 30)
	private String tag;

	public PokemonTagId() {
	}

	PokemonTagId(Integer pokemonId, String tag) {
		this.pokemonId = pokemonId;
		this.tag = tag;
	}

	String tag() {
		return tag;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PokemonTagId that)) {
			return false;
		}
		return Objects.equals(pokemonId, that.pokemonId) && Objects.equals(tag, that.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pokemonId, tag);
	}

}
