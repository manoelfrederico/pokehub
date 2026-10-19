package com.fred.pokehub.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ability")
public class AbilityEntity {

	@Id
	private Integer id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	static AbilityEntity create(int id, String name) {
		AbilityEntity ability = new AbilityEntity();
		ability.id = id;
		ability.name = name;
		return ability;
	}

	Integer id() {
		return id;
	}

	String name() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

}
