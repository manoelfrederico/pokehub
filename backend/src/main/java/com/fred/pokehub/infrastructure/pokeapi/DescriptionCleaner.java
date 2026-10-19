package com.fred.pokehub.infrastructure.pokeapi;

import java.util.List;
import java.util.regex.Pattern;

final class DescriptionCleaner {

	private static final String ENGLISH = "en";

	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private DescriptionCleaner() {
	}

	static String englishDescription(List<PokeApiResponses.FlavorText> entries) {
		if (entries == null) {
			return null;
		}
		String latest = null;
		for (PokeApiResponses.FlavorText entry : entries) {
			if (isEnglish(entry)) {
				latest = entry.flavorText();
			}
		}
		if (latest == null) {
			return null;
		}
		String cleaned = WHITESPACE.matcher(latest).replaceAll(" ").trim();
		return cleaned.isEmpty() ? null : cleaned;
	}

	private static boolean isEnglish(PokeApiResponses.FlavorText entry) {
		return entry != null
				&& entry.flavorText() != null
				&& entry.language() != null
				&& ENGLISH.equals(entry.language().name());
	}

}
