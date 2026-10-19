package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "pokemon_tag")
public class PokemonTagEntity {

	@EmbeddedId
	private PokemonTagId id = new PokemonTagId();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("pokemonId")
	@JoinColumn(name = "pokemon_id", nullable = false)
	private PokemonEntity pokemon;

	void assign(PokemonEntity pokemon, String tag) {
		this.pokemon = pokemon;
		this.id = new PokemonTagId(pokemon.id(), tag);
	}

	String tag() {
		return id.tag();
	}

}
