package com.fred.pokehub.infrastructure.persistence;

import com.fred.pokehub.domain.model.EvolutionNode;

import java.util.List;

record EvolutionChainDocument(int id, String name, String spriteUrl, List<EvolutionChainDocument> evolvesTo) {

	EvolutionChainDocument {
		evolvesTo = evolvesTo == null ? List.of() : List.copyOf(evolvesTo);
	}

	static EvolutionChainDocument from(EvolutionNode node) {
		List<EvolutionChainDocument> children = node.evolvesTo().stream().map(EvolutionChainDocument::from).toList();
		return new EvolutionChainDocument(node.id(), node.name(), node.spriteUrl(), children);
	}

	EvolutionNode toNode() {
		List<EvolutionNode> children = evolvesTo.stream().map(EvolutionChainDocument::toNode).toList();
		return new EvolutionNode(id, name, spriteUrl, children);
	}

}
