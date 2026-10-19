package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "species")
public class SpeciesEntity {

	@Id
	private Integer id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 200)
	private String category;

	@Column(columnDefinition = "text")
	private String description;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "evolution_chain")
	private EvolutionChainDocument evolutionChain;

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

	String category() {
		return category;
	}

	void setCategory(String category) {
		this.category = category;
	}

	String description() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	EvolutionChainDocument evolutionChain() {
		return evolutionChain;
	}

	void setEvolutionChain(EvolutionChainDocument evolutionChain) {
		this.evolutionChain = evolutionChain;
	}

}
