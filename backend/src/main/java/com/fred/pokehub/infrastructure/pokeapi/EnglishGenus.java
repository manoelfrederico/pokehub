package com.fred.pokehub.infrastructure.pokeapi;

final class EnglishGenus {

	private static final String ENGLISH = "en";

	private EnglishGenus() {
	}

	static String category(PokeApiResponses.Species species) {
		if (species == null || species.genera() == null) {
			return null;
		}
		for (PokeApiResponses.Genus genus : species.genera()) {
			if (isEnglish(genus)) {
				return genus.genus();
			}
		}
		return null;
	}

	private static boolean isEnglish(PokeApiResponses.Genus genus) {
		return genus != null && genus.language() != null && ENGLISH.equals(genus.language().name());
	}

}
