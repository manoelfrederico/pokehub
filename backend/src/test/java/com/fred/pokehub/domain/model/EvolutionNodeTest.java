package com.fred.pokehub.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvolutionNodeTest {

	@Test
	void shouldExposeIdentityNameSpriteAndChildren() {
		EvolutionNode child = new EvolutionNode(2, "ivysaur", "https://sprites/2.png", List.of());
		EvolutionNode root = new EvolutionNode(1, "  bulbasaur  ", "https://sprites/1.png", List.of(child));

		assertThat(root.id()).isEqualTo(1);
		assertThat(root.name()).isEqualTo("bulbasaur");
		assertThat(root.spriteUrl()).isEqualTo("https://sprites/1.png");
		assertThat(root.evolvesTo()).containsExactly(child);
	}

	@Test
	void shouldAllowANullSprite() {
		assertThat(new EvolutionNode(1, "bulbasaur", null, List.of()).spriteUrl()).isNull();
	}

	@Test
	void shouldRejectNonPositiveId() {
		assertThatThrownBy(() -> node(0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("0");
		assertThatThrownBy(() -> node(-1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("-1");
	}

	@Test
	void shouldRejectBlankName() {
		assertThatThrownBy(() -> new EvolutionNode(1, "  ", null, List.of()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullName() {
		assertThatThrownBy(() -> new EvolutionNode(1, null, null, List.of()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullChildren() {
		assertThatThrownBy(() -> new EvolutionNode(1, "bulbasaur", null, null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldCopyChildrenAndKeepEveryLevelImmutable() {
		List<EvolutionNode> grandchildren = new ArrayList<>();
		grandchildren.add(new EvolutionNode(3, "venusaur", null, List.of()));
		EvolutionNode child = new EvolutionNode(2, "ivysaur", null, grandchildren);
		List<EvolutionNode> children = new ArrayList<>();
		children.add(child);
		EvolutionNode root = new EvolutionNode(1, "bulbasaur", null, children);

		children.add(new EvolutionNode(4, "other", null, List.of()));
		grandchildren.add(new EvolutionNode(5, "other", null, List.of()));

		assertThat(root.evolvesTo()).containsExactly(child);
		assertThat(root.evolvesTo().getFirst().evolvesTo()).containsExactly(new EvolutionNode(3, "venusaur", null, List.of()));
		assertThatThrownBy(() -> root.evolvesTo().add(new EvolutionNode(4, "other", null, List.of())))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> root.evolvesTo().getFirst().evolvesTo().add(new EvolutionNode(5, "other", null, List.of())))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private static EvolutionNode node(int id) {
		return new EvolutionNode(id, "bulbasaur", null, List.of());
	}

}
