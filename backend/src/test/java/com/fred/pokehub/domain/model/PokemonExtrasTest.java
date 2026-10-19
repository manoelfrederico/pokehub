package com.fred.pokehub.domain.model;

import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidPokemonExtrasException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonExtrasTest {

	private static final String TEXT_TOO_LONG = "must be at most 100 characters";

	private static final String TOO_MANY_TAGS = "must contain at most 10 tags";

	private static final String TAG_TOO_LONG = "must be at most 30 characters";

	private static final String TAG_BLANK = "must not be blank";

	private static final String TAG_NULL = "must not be null";

	private static final String TAG_DUPLICATE = "must not contain duplicates";

	@Test
	void shouldAcceptNullTextAndANullTagList() {
		PokemonExtras extras = new PokemonExtras(null, null, null);

		assertThat(extras).isEqualTo(PokemonExtras.empty());
		assertThat(extras.localizedName()).isNull();
		assertThat(extras.region()).isNull();
		assertThat(extras.tags()).isEmpty();
	}

	@Test
	void shouldTurnBlankTextAndAnEmptyTagListIntoEmptyExtras() {
		PokemonExtras extras = new PokemonExtras("   ", "\t", List.of());

		assertThat(extras).isEqualTo(PokemonExtras.empty());
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "\t", " \n "})
	void shouldTurnBlankLocalizedNameIntoNull(String blank) {
		PokemonExtras extras = new PokemonExtras(blank, "Kanto", List.of("starter"));

		assertThat(extras.localizedName()).isNull();
		assertThat(extras.region()).isEqualTo("Kanto");
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "\t", " \n "})
	void shouldTurnBlankRegionIntoNull(String blank) {
		PokemonExtras extras = new PokemonExtras("Bulbassauro", blank, List.of("starter"));

		assertThat(extras.region()).isNull();
		assertThat(extras.localizedName()).isEqualTo("Bulbassauro");
	}

	@Test
	void shouldTrimLocalizedNameAndRegion() {
		PokemonExtras extras = new PokemonExtras("  Bulbassauro  ", "\tKanto\n", List.of("starter"));

		assertThat(extras.localizedName()).isEqualTo("Bulbassauro");
		assertThat(extras.region()).isEqualTo("Kanto");
	}

	@Test
	void shouldAcceptTextAtTheMaximumLength() {
		String maximum = "a".repeat(100);

		PokemonExtras extras = new PokemonExtras("  " + maximum + "  ", "\t" + maximum + "\n", List.of());

		assertThat(extras.localizedName()).isEqualTo(maximum);
		assertThat(extras.region()).isEqualTo(maximum);
	}

	@ParameterizedTest
	@MethodSource("textLongerThanTheMaximum")
	void shouldRejectTextLongerThanTheMaximum(String localizedName, String region, String field) {
		assertThatThrownBy(() -> new PokemonExtras(localizedName, region, List.of()))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception -> {
					assertThat(exception).hasMessage("Pokémon extras are invalid.");
					assertThat(exception.getMessage()).doesNotContain("a".repeat(101));
					assertThat(exception.violations()).containsExactly(new FieldViolation(field, TEXT_TOO_LONG));
					assertThat(exception.violations().getFirst().message()).doesNotContain("a".repeat(101));
				});
	}

	@Test
	void shouldAcceptZeroOneAndTenTags() {
		assertThat(new PokemonExtras(null, null, List.of()).tags()).isEmpty();
		assertThat(new PokemonExtras(null, null, List.of("starter")).tags()).containsExactly("starter");
		assertThat(new PokemonExtras(null, null, tenTags()).tags()).containsExactlyElementsOf(tenTags());
	}

	@Test
	void shouldRejectElevenTags() {
		List<String> tags = new ArrayList<>(tenTags());
		tags.add("eleven");

		assertThatThrownBy(() -> new PokemonExtras(null, null, tags))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception ->
						assertThat(exception.violations()).containsExactly(new FieldViolation("tags", TOO_MANY_TAGS)));
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 30})
	void shouldAcceptATagAtTheLengthBoundary(int length) {
		String tag = "a".repeat(length);

		PokemonExtras extras = new PokemonExtras(null, null, List.of("  " + tag + "  "));

		assertThat(extras.tags()).containsExactly(tag);
	}

	@Test
	void shouldRejectATagLongerThanThirtyCharacters() {
		String tag = "b".repeat(31);

		assertThatThrownBy(() -> new PokemonExtras(null, null, List.of("  " + tag + " ")))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception -> {
					assertThat(exception.violations()).containsExactly(new FieldViolation("tags[0]", TAG_TOO_LONG));
					assertThat(exception.violations().getFirst().message()).doesNotContain(tag);
				});
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "\t", "  \n"})
	void shouldRejectABlankTag(String tag) {
		assertThatThrownBy(() -> new PokemonExtras(null, null, List.of(tag)))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception ->
						assertThat(exception.violations()).containsExactly(new FieldViolation("tags[0]", TAG_BLANK)));
	}

	@Test
	void shouldLowerCaseAndTrimTags() {
		PokemonExtras extras = new PokemonExtras(null, null, List.of("  StArTeR ", "FIRE"));

		assertThat(extras.tags()).containsExactly("starter", "fire");
	}

	@Test
	void shouldRejectTagsThatDifferOnlyByCase() {
		assertThatThrownBy(() -> new PokemonExtras(null, null, List.of("Grass", "grass")))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception -> {
					assertThat(exception.violations()).containsExactly(new FieldViolation("tags", TAG_DUPLICATE));
					assertThat(exception.getMessage()).doesNotContain("Grass");
				});
	}

	@Test
	void shouldRejectTagsThatDifferOnlyByWhitespace() {
		assertThatThrownBy(() -> new PokemonExtras(null, null, List.of(" grass ", "GRASS")))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception ->
						assertThat(exception.violations()).containsExactly(new FieldViolation("tags", TAG_DUPLICATE)));
	}

	@Test
	void shouldRejectANullTag() {
		assertThatThrownBy(() -> new PokemonExtras(null, null, java.util.Arrays.asList("starter", null)))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception ->
						assertThat(exception.violations()).containsExactly(new FieldViolation("tags[1]", TAG_NULL)));
	}

	@Test
	void shouldCopyTagsAndRejectLaterMutation() {
		List<String> tags = new ArrayList<>();
		tags.add("  STARTER ");
		PokemonExtras extras = new PokemonExtras("Bulbassauro", "Kanto", tags);

		tags.add("grass");

		assertThat(extras.tags()).containsExactly("starter");
		assertThatThrownBy(() -> extras.tags().add("fire"))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void shouldReportEveryViolationTogether() {
		List<String> tags = new ArrayList<>(tenTags());
		tags.set(0, null);
		tags.set(1, "  ");
		tags.set(2, "c".repeat(31));
		tags.set(3, "Grass");
		tags.add(" grass ");

		assertThatThrownBy(() -> new PokemonExtras("n".repeat(101), "r".repeat(101), tags))
				.isInstanceOfSatisfying(InvalidPokemonExtrasException.class, exception -> {
					assertThat(exception.violations()).containsExactly(
							new FieldViolation("localizedName", TEXT_TOO_LONG),
							new FieldViolation("region", TEXT_TOO_LONG),
							new FieldViolation("tags", TOO_MANY_TAGS),
							new FieldViolation("tags[0]", TAG_NULL),
							new FieldViolation("tags[1]", TAG_BLANK),
							new FieldViolation("tags[2]", TAG_TOO_LONG),
							new FieldViolation("tags", TAG_DUPLICATE));
					assertThatThrownBy(() -> exception.violations().clear())
							.isInstanceOf(UnsupportedOperationException.class);
				});
	}

	@Test
	void shouldRejectAnEmptyViolationList() {
		assertThatThrownBy(() -> new InvalidPokemonExtrasException(List.of()))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new InvalidPokemonExtrasException(null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@MethodSource("blankViolationParts")
	void shouldRejectABlankViolationPart(String field, String message) {
		assertThatThrownBy(() -> new FieldViolation(field, message))
				.isInstanceOf(IllegalArgumentException.class);
	}

	private static Stream<Arguments> textLongerThanTheMaximum() {
		String tooLong = "a".repeat(101);
		String padded = "  " + tooLong + "  ";
		return Stream.of(
				Arguments.of(tooLong, "Kanto", "localizedName"),
				Arguments.of("Kanto", tooLong, "region"),
				Arguments.of(padded, "Kanto", "localizedName"));
	}

	private static Stream<Arguments> blankViolationParts() {
		return Stream.of(
				Arguments.of(null, "must not be blank"),
				Arguments.of(" ", "must not be blank"),
				Arguments.of("tags", null),
				Arguments.of("tags", "  "));
	}

	private static List<String> tenTags() {
		return List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten");
	}

}
