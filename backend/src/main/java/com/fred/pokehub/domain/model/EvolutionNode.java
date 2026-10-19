package com.fred.pokehub.domain.model;

import java.util.List;

/**
 * One node of an evolutionary lineage. {@code spriteUrl} may be null. Children are copied.
 */
public record EvolutionNode(int id, String name, String spriteUrl, List<EvolutionNode> evolvesTo) {

	public EvolutionNode {
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive but was " + id);
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (evolvesTo == null) {
			throw new IllegalArgumentException("evolvesTo must not be null");
		}
		name = name.trim();
		evolvesTo = List.copyOf(evolvesTo);
	}

}
