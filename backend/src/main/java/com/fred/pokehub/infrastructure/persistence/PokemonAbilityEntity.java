package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "pokemon_ability")
public class PokemonAbilityEntity {

	@EmbeddedId
	private PokemonAbilityId id = new PokemonAbilityId();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("pokemonId")
	@JoinColumn(name = "pokemon_id", nullable = false)
	private PokemonEntity pokemon;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("abilityId")
	@JoinColumn(name = "ability_id", nullable = false)
	private AbilityEntity ability;

	@Column(nullable = false)
	private boolean hidden;

	void assign(PokemonEntity pokemon, AbilityEntity ability, boolean hidden) {
		this.pokemon = pokemon;
		this.ability = ability;
		this.hidden = hidden;
		this.id = new PokemonAbilityId(pokemon.id(), ability.id());
	}

	AbilityEntity ability() {
		return ability;
	}

	boolean hidden() {
		return hidden;
	}

}
