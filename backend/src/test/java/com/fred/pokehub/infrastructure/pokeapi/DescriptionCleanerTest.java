package com.fred.pokehub.infrastructure.pokeapi;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescriptionCleanerTest {

	@Test
	void shouldKeepTheLastEnglishEntry() {
		List<PokeApiResponses.FlavorText> entries = List.of(
				entry("First English.", "en"),
				entry("Texte français.", "fr"),
				entry("  Last\nEnglish.\fDone.  ", "en"));

		assertThat(DescriptionCleaner.englishDescription(entries)).isEqualTo("Last English. Done.");
	}

	@Test
	void shouldCollapseNewlinesAndFormFeedsAndTrim() {
		String raw = "A strange seed was\nplanted on its\nback at birth.\fThe plant sprouts\nand grows with\nthis POKéMON.";

		assertThat(DescriptionCleaner.englishDescription(List.of(entry("  " + raw + "  ", "en"))))
				.isEqualTo("A strange seed was planted on its back at birth. The plant sprouts and grows with this POKéMON.");
	}

	@Test
	void shouldReturnNullWhenNoEnglishEntryExists() {
		assertThat(DescriptionCleaner.englishDescription(List.of(entry("Bonjour.", "fr")))).isNull();
	}

	@Test
	void shouldReturnNullWhenTheListIsNull() {
		assertThat(DescriptionCleaner.englishDescription(null)).isNull();
	}

	@Test
	void shouldReturnNullWhenTheEnglishTextIsBlank() {
		assertThat(DescriptionCleaner.englishDescription(List.of(entry(" \n\f ", "en")))).isNull();
	}

	@Test
	void shouldIgnoreEntriesWithoutEnglishText() {
		assertThat(DescriptionCleaner.englishDescription(Arrays.asList(
				null,
				entry(null, "en"),
				new PokeApiResponses.FlavorText("Hello.", null),
				entry("Kept.", "en")))).isEqualTo("Kept.");
	}

	private static PokeApiResponses.FlavorText entry(String text, String language) {
		return new PokeApiResponses.FlavorText(text, new PokeApiResponses.Language(language));
	}

}
