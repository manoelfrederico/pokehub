package com.fred.pokehub.web;

import com.fred.pokehub.application.port.in.AuthenticateUserUseCase;
import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.application.port.in.SyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UnsyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasUseCase;
import com.fred.pokehub.web.auth.AuthController;
import com.fred.pokehub.web.pokemon.PokemonController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PokemonController.class, AuthController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(OpenApiConfig.class)
@ImportAutoConfiguration({
		SpringDocConfiguration.class,
		SpringDocConfigProperties.class,
		SpringDocWebMvcConfiguration.class
})
class OpenApiContractTest {

	private static final JsonMapper JSON = JsonMapper.builder().build();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ListPokemonUseCase listPokemon;

	@MockitoBean
	private GetPokemonDetailsUseCase pokemonDetails;

	@MockitoBean
	private SyncPokemonUseCase syncPokemon;

	@MockitoBean
	private UnsyncPokemonUseCase unsyncPokemon;

	@MockitoBean
	private UpdatePokemonExtrasUseCase updatePokemonExtras;

	@MockitoBean
	private RegisterUserUseCase registerUser;

	@MockitoBean
	private AuthenticateUserUseCase authenticateUser;

	@ParameterizedTest
	@MethodSource("publicRoutes")
	void shouldPublishPublicRoutesWithoutSecurity(String method, String path, Set<String> statuses) throws Exception {
		JsonNode operation = operation(method, path);

		assertThat(operation.isMissingNode()).isFalse();
		assertThat(schemeNames(operation)).isEmpty();
		assertThat(statusCodes(operation)).containsExactlyInAnyOrderElementsOf(statuses);
	}

	@ParameterizedTest
	@MethodSource("protectedRoutes")
	void shouldPublishProtectedRoutesWithBearer(String method, String path, Set<String> statuses) throws Exception {
		JsonNode operation = operation(method, path);

		assertThat(operation.isMissingNode()).isFalse();
		assertThat(schemeNames(operation)).containsExactly("bearer");
		assertThat(statusCodes(operation)).containsExactlyInAnyOrderElementsOf(statuses);
	}

	@Test
	void shouldDefineHttpBearerJwtScheme() throws Exception {
		JsonNode scheme = document().path("components").path("securitySchemes").path("bearer");

		assertThat(scheme.isMissingNode()).isFalse();
		assertThat(scheme.path("type").asString()).isEqualTo("http");
		assertThat(scheme.path("scheme").asString()).isEqualTo("bearer");
		assertThat(scheme.path("bearerFormat").asString()).isEqualTo("JWT");
	}

	static Stream<Arguments> publicRoutes() {
		return Stream.of(
				arguments("get", "/api/v1/pokemon", Set.of("200", "400", "502", "500")),
				arguments("get", "/api/v1/pokemon/{id}", Set.of("200", "400", "404", "502", "500")),
				arguments("post", "/api/v1/auth/register", Set.of("201", "400", "409", "500")),
				arguments("post", "/api/v1/auth/login", Set.of("200", "400", "401", "500")));
	}

	static Stream<Arguments> protectedRoutes() {
		return Stream.of(
				arguments("post", "/api/v1/pokemon/{id}/sync", Set.of("200", "201", "400", "401", "404", "502", "500")),
				arguments("delete", "/api/v1/pokemon/{id}/sync", Set.of("204", "400", "401", "404", "500")),
				arguments("put", "/api/v1/pokemon/{id}/extras", Set.of("200", "400", "401", "404", "415", "500")));
	}

	private JsonNode operation(String method, String path) throws Exception {
		return document().path("paths").path(path).path(method);
	}

	private JsonNode document() throws Exception {
		String body = mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return JSON.readTree(body);
	}

	private static Set<String> statusCodes(JsonNode operation) {
		return Set.copyOf(operation.path("responses").propertyNames());
	}

	private static List<String> schemeNames(JsonNode operation) {
		JsonNode security = operation.get("security");
		if (security == null || security.isEmpty()) {
			return List.of();
		}
		return security.valueStream()
				.flatMap(requirement -> requirement.propertyNames().stream())
				.toList();
	}

}
