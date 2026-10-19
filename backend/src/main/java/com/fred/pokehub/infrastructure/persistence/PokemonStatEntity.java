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
@Table(name = "pokemon_stat")
public class PokemonStatEntity {

	@EmbeddedId
	private PokemonStatId id = new PokemonStatId();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("pokemonId")
	@JoinColumn(name = "pokemon_id", nullable = false)
	private PokemonEntity pokemon;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(name = "base_value", nullable = false)
	private int baseValue;

	void assign(PokemonEntity pokemon, short position, String name, int baseValue) {
		this.pokemon = pokemon;
		this.name = name;
		this.baseValue = baseValue;
		this.id = new PokemonStatId(pokemon.id(), position);
	}

	short position() {
		return id.position();
	}

	String name() {
		return name;
	}

	int baseValue() {
		return baseValue;
	}

}
