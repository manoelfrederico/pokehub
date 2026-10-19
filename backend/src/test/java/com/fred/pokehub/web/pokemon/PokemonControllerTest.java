package com.fred.pokehub.web.pokemon;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.exception.PokemonNotFoundException;
import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.PokemonPage;
import com.fred.pokehub.domain.model.PokemonSummary;
import com.fred.pokehub.domain.model.PokemonView;
import com.fred.pokehub.domain.model.Stat;
import com.fred.pokehub.support.PokemonSamples;
import com.fred.pokehub.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PokemonControllerTest {

	private static final String BASE = "http://localhost/api/v1/pokemon";

	private FakeListPokemon useCase;

	private FakeDetails details;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		useCase = new FakeListPokemon();
		details = new FakeDetails();
		mockMvc = MockMvcBuilders.standaloneSetup(new PokemonController(useCase, details))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setMessageConverters(new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
				.build();
	}

	@Test
	void shouldApplyDefaultPaginationWhenQueryParamsAreMissing() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon")).andExpect(status().isOk());

		assertThat(useCase.received).isEqualTo(PageQuery.of(null, null));
	}

	@Test
	void shouldPassCustomPaginationToTheUseCase() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon").param("limit", "10").param("offset", "5"))
				.andExpect(status().isOk());

		assertThat(useCase.received).isEqualTo(PageQuery.of(10, 5));
	}

	@Test
	void shouldReturnTheContractShape() throws Exception {
		useCase.items = List.of(PokemonSamples.bulbasaur());

		mockMvc.perform(get("/api/v1/pokemon"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.count").value(1351))
				.andExpect(jsonPath("$.next").value(BASE + "?limit=20&offset=20"))
				.andExpect(jsonPath("$.previous").value(nullValue()))
				.andExpect(jsonPath("$.results[0].id").value(1))
				.andExpect(jsonPath("$.results[0].name").value("bulbasaur"))
				.andExpect(jsonPath("$.results[0].sprite")
						.value("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"))
				.andExpect(jsonPath("$.results[0].category").value("Seed Pokémon"))
				.andExpect(jsonPath("$.results[0].mass").isNumber())
				.andExpect(jsonPath("$.results[0].mass").value(6.9))
				.andExpect(jsonPath("$.results[0].skills[0].name").value("overgrow"))
				.andExpect(jsonPath("$.results[0].skills[0].hidden").value(false))
				.andExpect(jsonPath("$.results[0].skills[1].name").value("chlorophyll"))
				.andExpect(jsonPath("$.results[0].skills[1].hidden").value(true))
				.andExpect(jsonPath("$.results[0].synced").doesNotExist());
	}

	@Test
	void shouldSerializeNullSpriteAndCategoryAsNull() throws Exception {
		useCase.items = List.of(PokemonSamples.summary(4));

		mockMvc.perform(get("/api/v1/pokemon"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results[0].sprite").value(nullValue()))
				.andExpect(jsonPath("$.results[0].category").value(nullValue()));
	}

	@Test
	void shouldBuildNextAndPreviousForAMiddlePage() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon").param("limit", "20").param("offset", "40"))
				.andExpect(jsonPath("$.next").value(BASE + "?limit=20&offset=60"))
				.andExpect(jsonPath("$.previous").value(BASE + "?limit=20&offset=20"));
	}

	@Test
	void shouldOmitNextOnTheLastPage() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon").param("limit", "20").param("offset", "1340"))
				.andExpect(jsonPath("$.next").value(nullValue()))
				.andExpect(jsonPath("$.previous").value(BASE + "?limit=20&offset=1320"));
	}

	@Test
	void shouldOmitNextWhenTheOffsetIsBeyondTheEnd() throws Exception {
		useCase.count = 10;

		mockMvc.perform(get("/api/v1/pokemon").param("limit", "20").param("offset", "50"))
				.andExpect(jsonPath("$.next").value(nullValue()))
				.andExpect(jsonPath("$.previous").value(BASE + "?limit=20&offset=30"));
	}

	@ParameterizedTest
	@CsvSource({
			"limit, 0, 0",
			"limit, 51, 51",
			"limit, -1, -1",
			"offset, -1, -1"
	})
	void shouldReturnBadRequestForInvalidPagination(String name, String value, String offending) throws Exception {
		mockMvc.perform(get("/api/v1/pokemon").param(name, value))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Bad request"))
				.andExpect(jsonPath("$.detail").value(containsString(offending)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"limit", "offset"})
	void shouldReturnBadRequestForNonNumericPagination(String name) throws Exception {
		mockMvc.perform(get("/api/v1/pokemon").param(name, "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").isNotEmpty())
				.andExpect(jsonPath("$.detail").value(containsString("abc")));
	}

	@Test
	void shouldReturnBadGatewayWithoutInternalDetail() throws Exception {
		useCase.failure = new CatalogUnavailableException("postgres password leaked", new IOException("secret-reset"));

		mockMvc.perform(get("/api/v1/pokemon"))
				.andExpect(status().isBadGateway())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(502))
				.andExpect(jsonPath("$.title").value("Catalog unavailable"))
				.andExpect(jsonPath("$.detail").value("The Pokémon catalog is temporarily unavailable."))
				.andExpect(content().string(org.hamcrest.Matchers.not(containsString("password"))))
				.andExpect(content().string(org.hamcrest.Matchers.not(containsString("secret-reset"))));
	}

	@Test
	void shouldReturnInternalErrorWithoutTheUnexpectedMessage() throws Exception {
		useCase.failure = new IllegalStateException("boom-secret-xyz");

		mockMvc.perform(get("/api/v1/pokemon"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.title").value("Internal error"))
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
				.andExpect(content().string(org.hamcrest.Matchers.not(containsString("boom-secret-xyz"))));
	}

	@Test
	void shouldRejectPost() throws Exception {
		mockMvc.perform(post("/api/v1/pokemon"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(405))
				.andExpect(jsonPath("$.title").isNotEmpty())
				.andExpect(jsonPath("$.detail").isNotEmpty());
	}

	@Test
	void shouldReturnTheDetailContract() throws Exception {
		details.view = bulbasaurView(null, false);

		mockMvc.perform(get("/api/v1/pokemon/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("bulbasaur"))
				.andExpect(jsonPath("$.image").value("https://images/official-artwork/1.png"))
				.andExpect(jsonPath("$.sprite").value("https://sprites/1.png"))
				.andExpect(jsonPath("$.category").value("Seed Pokémon"))
				.andExpect(jsonPath("$.mass").value(6.9))
				.andExpect(jsonPath("$.skills[0].name").value("overgrow"))
				.andExpect(jsonPath("$.skills[0].hidden").value(false))
				.andExpect(jsonPath("$.stats[0].name").value("hp"))
				.andExpect(jsonPath("$.stats[0].baseValue").value(45))
				.andExpect(jsonPath("$.stats[1].name").value("attack"))
				.andExpect(jsonPath("$.stats[1].baseValue").value(49))
				.andExpect(jsonPath("$.description").value("A strange seed."))
				.andExpect(jsonPath("$.evolutionChain.id").value(1))
				.andExpect(jsonPath("$.evolutionChain.name").value("bulbasaur"))
				.andExpect(jsonPath("$.evolutionChain.sprite").value("https://sprites/1.png"))
				.andExpect(jsonPath("$.evolutionChain.evolvesTo[0].id").value(2))
				.andExpect(jsonPath("$.evolutionChain.evolvesTo[0].name").value("ivysaur"))
				.andExpect(jsonPath("$.evolutionChain.evolvesTo[0].evolvesTo[0].id").value(3))
				.andExpect(jsonPath("$.evolutionChain.evolvesTo[0].evolvesTo[0].name").value("venusaur"))
				.andExpect(jsonPath("$.evolutionChain.evolvesTo[0].evolvesTo[0].evolvesTo").isEmpty())
				.andExpect(jsonPath("$.synced").value(false))
				.andExpect(jsonPath("$.extras").value(nullValue()));

		assertThat(details.requestedId).isEqualTo(1);
	}

	@Test
	void shouldSerializeNullDetailFields() throws Exception {
		details.view = new PokemonView(
				new PokemonDetails(4, "pokemon-4", null, null, null, new Mass(10), List.of(), List.of(), null,
						new EvolutionNode(4, "pokemon-4", null, List.of())),
				null,
				false);

		mockMvc.perform(get("/api/v1/pokemon/4"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.image").value(nullValue()))
				.andExpect(jsonPath("$.sprite").value(nullValue()))
				.andExpect(jsonPath("$.category").value(nullValue()))
				.andExpect(jsonPath("$.description").value(nullValue()))
				.andExpect(jsonPath("$.evolutionChain.sprite").value(nullValue()))
				.andExpect(jsonPath("$.skills").isEmpty())
				.andExpect(jsonPath("$.stats").isEmpty());
	}

	@Test
	void shouldSerializeExtrasWhenThePokemonIsSynced() throws Exception {
		PokemonExtras extras = new PokemonExtras("Bulbassauro", "Kanto", List.of("starter", "grass"));
		details.view = bulbasaurView(extras, true);

		mockMvc.perform(get("/api/v1/pokemon/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.synced").value(true))
				.andExpect(jsonPath("$.extras.localizedName").value("Bulbassauro"))
				.andExpect(jsonPath("$.extras.region").value("Kanto"))
				.andExpect(jsonPath("$.extras.tags[0]").value("starter"))
				.andExpect(jsonPath("$.extras.tags[1]").value("grass"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "-1"})
	void shouldReturnBadRequestForANonPositiveId(String id) throws Exception {
		mockMvc.perform(get("/api/v1/pokemon/" + id))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Bad request"))
				.andExpect(jsonPath("$.detail").value(containsString(id)));

		assertThat(details.requestedId).isEqualTo(Integer.valueOf(id));
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "2147483648"})
	void shouldReturnBadRequestForAnUnparseableId(String id) throws Exception {
		mockMvc.perform(get("/api/v1/pokemon/" + id))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").isNotEmpty())
				.andExpect(jsonPath("$.detail").value(containsString(id)));

		assertThat(details.requestedId).isNull();
	}

	@Test
	void shouldReturnNotFoundWhenThePokemonDoesNotExist() throws Exception {
		details.failure = new PokemonNotFoundException(999999);

		mockMvc.perform(get("/api/v1/pokemon/999999"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Pokémon not found"))
				.andExpect(jsonPath("$.detail").value(containsString("999999")));
	}

	@Test
	void shouldReturnBadGatewayForAnUnavailableDetail() throws Exception {
		details.failure = new CatalogUnavailableException("postgres password leaked", new IOException("secret-reset"));

		mockMvc.perform(get("/api/v1/pokemon/1"))
				.andExpect(status().isBadGateway())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(502))
				.andExpect(jsonPath("$.title").value("Catalog unavailable"))
				.andExpect(jsonPath("$.detail").value("The Pokémon catalog is temporarily unavailable."))
				.andExpect(content().string(org.hamcrest.Matchers.not(containsString("password"))))
				.andExpect(content().string(org.hamcrest.Matchers.not(containsString("secret-reset"))));
	}

	@Test
	void shouldReturnInternalErrorForAnUnexpectedDetailFailure() throws Exception {
		details.failure = new IllegalStateException("boom-secret-xyz");

		mockMvc.perform(get("/api/v1/pokemon/1"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.title").value("Internal error"))
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
				.andExpect(content().string(org.hamcrest.Matchers.not(containsString("boom-secret-xyz"))));
	}

	@Test
	void shouldFollowSpringDefaultsForATrailingSlashAndAnotherVerb() throws Exception {
		mockMvc.perform(post("/api/v1/pokemon/1"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(405))
				.andExpect(jsonPath("$.title").isNotEmpty());

		mockMvc.perform(get("/api/v1/pokemon/1/"))
				.andExpect(status().isNotFound());
	}

	private static PokemonView bulbasaurView(PokemonExtras extras, boolean synced) {
		EvolutionNode venusaur = new EvolutionNode(3, "venusaur", "https://sprites/3.png", List.of());
		EvolutionNode ivysaur = new EvolutionNode(2, "ivysaur", "https://sprites/2.png", List.of(venusaur));
		EvolutionNode bulbasaur = new EvolutionNode(1, "bulbasaur", "https://sprites/1.png", List.of(ivysaur));
		PokemonDetails snapshot = new PokemonDetails(
				1,
				"bulbasaur",
				"https://images/official-artwork/1.png",
				"https://sprites/1.png",
				"Seed Pokémon",
				new Mass(69),
				List.of(new Ability("overgrow", false)),
				List.of(new Stat("hp", 45), new Stat("attack", 49)),
				"A strange seed.",
				bulbasaur);
		return new PokemonView(snapshot, extras, synced);
	}

	private static final class FakeListPokemon implements ListPokemonUseCase {

		private int count = 1351;

		private List<PokemonSummary> items = List.of();

		private RuntimeException failure;

		private PageQuery received;

		@Override
		public PokemonPage list(PageQuery query) {
			received = query;
			if (failure != null) {
				throw failure;
			}
			return new PokemonPage(count, query, items);
		}

	}

	private static final class FakeDetails implements GetPokemonDetailsUseCase {

		private PokemonView view;

		private RuntimeException failure;

		private Integer requestedId;

		@Override
		public PokemonView get(int id) {
			requestedId = id;
			if (id < 1) {
				throw new InvalidPokemonIdException("id must be positive but was " + id);
			}
			if (failure != null) {
				throw failure;
			}
			return view;
		}

	}

}
