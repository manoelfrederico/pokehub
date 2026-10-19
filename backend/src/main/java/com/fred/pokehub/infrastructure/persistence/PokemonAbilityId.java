package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PokemonAbilityId implements Serializable {

	@Column(name = "pokemon_id")
	private Integer pokemonId;

	@Column(name = "ability_id")
	private Integer abilityId;

	public PokemonAbilityId() {
	}

	PokemonAbilityId(Integer pokemonId, Integer abilityId) {
		this.pokemonId = pokemonId;
		this.abilityId = abilityId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PokemonAbilityId that)) {
			return false;
		}
		return Objects.equals(pokemonId, that.pokemonId) && Objects.equals(abilityId, that.abilityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pokemonId, abilityId);
	}

}
