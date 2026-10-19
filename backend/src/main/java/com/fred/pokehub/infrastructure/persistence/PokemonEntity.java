package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemon")
public class PokemonEntity {

	@Id
	private Integer id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "sprite_url", length = 500)
	private String spriteUrl;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "weight_hg", nullable = false)
	private int weightHg;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "species_id", nullable = false)
	private SpeciesEntity species;

	@Column(name = "localized_name", length = 100)
	private String localizedName;

	@Column(length = 100)
	private String region;

	@OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PokemonAbilityEntity> abilities = new ArrayList<>();

	@OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id.position ASC")
	private List<PokemonStatEntity> stats = new ArrayList<>();

	@OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PokemonTagEntity> tags = new ArrayList<>();

	Integer id() {
		return id;
	}

	void setId(Integer id) {
		this.id = id;
	}

	String name() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	String spriteUrl() {
		return spriteUrl;
	}

	void setSpriteUrl(String spriteUrl) {
		this.spriteUrl = spriteUrl;
	}

	String imageUrl() {
		return imageUrl;
	}

	void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	int weightHg() {
		return weightHg;
	}

	void setWeightHg(int weightHg) {
		this.weightHg = weightHg;
	}

	SpeciesEntity species() {
		return species;
	}

	void setSpecies(SpeciesEntity species) {
		this.species = species;
	}

	String localizedName() {
		return localizedName;
	}

	void setLocalizedName(String localizedName) {
		this.localizedName = localizedName;
	}

	String region() {
		return region;
	}

	void setRegion(String region) {
		this.region = region;
	}

	List<PokemonAbilityEntity> abilities() {
		return abilities;
	}

	List<PokemonStatEntity> stats() {
		return stats;
	}

	List<PokemonTagEntity> tags() {
		return tags;
	}

}
