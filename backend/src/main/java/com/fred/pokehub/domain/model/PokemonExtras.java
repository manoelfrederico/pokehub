package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidPokemonExtrasException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Proprietary fields stored with a synced Pokémon. Text is trimmed and blank becomes null.
 * Tags are trimmed, lower-cased, and limited to ten unique values.
 */
public record PokemonExtras(String localizedName, String region, List<String> tags) {

	public static final int MAX_TEXT_LENGTH = 100;

	public static final int MAX_TAG_COUNT = 10;

	public static final int MAX_TAG_LENGTH = 30;

	private static final String TEXT_TOO_LONG = "must be at most 100 characters";

	private static final String TOO_MANY_TAGS = "must contain at most 10 tags";

	private static final String TAG_TOO_LONG = "must be at most 30 characters";

	private static final String TAG_BLANK = "must not be blank";

	private static final String TAG_NULL = "must not be null";

	private static final String TAG_DUPLICATE = "must not contain duplicates";

	public static PokemonExtras empty() {
		return new PokemonExtras(null, null, List.of());
	}

	public PokemonExtras {
		List<FieldViolation> violations = new ArrayList<>();
		localizedName = normalizedText(localizedName, "localizedName", violations);
		region = normalizedText(region, "region", violations);
		tags = normalizedTags(tags, violations);
		if (!violations.isEmpty()) {
			throw new InvalidPokemonExtrasException(violations);
		}
	}

	private static String normalizedText(String value, String field, List<FieldViolation> violations) {
		if (value == null) {
			return null;
		}
		String stripped = value.strip();
		if (stripped.isEmpty()) {
			return null;
		}
		if (stripped.length() > MAX_TEXT_LENGTH) {
			violations.add(new FieldViolation(field, TEXT_TOO_LONG));
			return null;
		}
		return stripped;
	}

	private static List<String> normalizedTags(List<String> tags, List<FieldViolation> violations) {
		if (tags == null) {
			return List.of();
		}
		if (tags.size() > MAX_TAG_COUNT) {
			violations.add(new FieldViolation("tags", TOO_MANY_TAGS));
		}
		return collectTags(tags, violations);
	}

	private static List<String> collectTags(List<String> tags, List<FieldViolation> violations) {
		List<String> normalized = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		boolean duplicate = false;
		for (int index = 0; index < tags.size(); index++) {
			duplicate = collectTag(tags.get(index), index, normalized, seen, violations) || duplicate;
		}
		if (duplicate) {
			violations.add(new FieldViolation("tags", TAG_DUPLICATE));
		}
		return List.copyOf(normalized);
	}

	private static boolean collectTag(
			String tag,
			int index,
			List<String> normalized,
			Set<String> seen,
			List<FieldViolation> violations
	) {
		String value = normalizedTag(tag, index, violations);
		if (value == null) {
			return false;
		}
		if (value.length() > MAX_TAG_LENGTH) {
			violations.add(new FieldViolation(tagField(index), TAG_TOO_LONG));
		}
		if (!seen.add(value)) {
			return true;
		}
		if (value.length() <= MAX_TAG_LENGTH) {
			normalized.add(value);
		}
		return false;
	}

	private static String normalizedTag(String tag, int index, List<FieldViolation> violations) {
		if (tag == null) {
			violations.add(new FieldViolation(tagField(index), TAG_NULL));
			return null;
		}
		String value = tag.strip().toLowerCase(Locale.ROOT);
		if (value.isEmpty()) {
			violations.add(new FieldViolation(tagField(index), TAG_BLANK));
			return null;
		}
		return value;
	}

	private static String tagField(int index) {
		return "tags[" + index + "]";
	}

}
