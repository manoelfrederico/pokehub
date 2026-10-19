package com.fred.pokehub.infrastructure.security;

import com.fred.pokehub.application.port.in.AuthenticateUserUseCase;
import com.fred.pokehub.application.port.in.GetPokemonDetailsUseCase;
import com.fred.pokehub.application.port.in.ListPokemonUseCase;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.application.port.in.SyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UnsyncPokemonUseCase;
import com.fred.pokehub.application.port.in.UpdatePokemonExtrasUseCase;
import com.fred.pokehub.config.JsonMapperConfig;
import com.fred.pokehub.domain.model.Ability;
import com.fred.pokehub.domain.model.EvolutionNode;
import com.fred.pokehub.domain.model.Mass;
import com.fred.pokehub.domain.model.PageQuery;
import com.fred.pokehub.domain.model.PokemonDetails;
import com.fred.pokehub.domain.model.PokemonExtras;
import com.fred.pokehub.domain.model.PokemonPage;
import com.fred.pokehub.domain.model.PokemonView;
import com.fred.pokehub.domain.model.Stat;
import com.fred.pokehub.domain.model.SyncResult;
import com.fred.pokehub.domain.model.User;
import com.fred.pokehub.web.auth.AuthController;
import com.fred.pokehub.web.pokemon.PokemonController;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PokemonController.class, AuthController.class})
@Import({SecurityConfig.class, JwtConfig.class, JsonMapperConfig.class})
class RouteSecurityTest {

	private static final User ASH = new User(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"ash",
			"hashed");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder;

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

	@BeforeEach
	void stubUseCases() {
		when(listPokemon.list(any(PageQuery.class)))
				.thenReturn(new PokemonPage(0, PageQuery.of(20, 0), List.of()));
		PokemonView view = new PokemonView(details(), null, false);
		when(pokemonDetails.get(anyInt())).thenReturn(view);
		when(syncPokemon.sync(anyInt())).thenReturn(new SyncResult(true, new PokemonView(details(), PokemonExtras.empty(), true)));
		when(updatePokemonExtras.update(any())).thenReturn(new PokemonView(details(), PokemonExtras.empty(), true));
		when(registerUser.register(any(), any())).thenReturn(ASH);
		when(authenticateUser.authenticate(any(), any()))
				.thenReturn(new com.fred.pokehub.application.port.out.AccessToken("jwt-value", 3600));
	}

	@Test
	void shouldAllowPublicReadsWithoutAToken() throws Exception {
		mockMvc.perform(get("/api/v1/pokemon")).andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/pokemon/1")).andExpect(status().isOk());
	}

	@Test
	void shouldAllowAuthAndToolingRoutesWithoutAToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"Pikachu#2026"}
								"""))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"Pikachu#2026"}
								"""))
				.andExpect(status().isOk());
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().is(not(401))).andExpect(status().is(not(403)));
		mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().is(not(401))).andExpect(status().is(not(403)));
		mockMvc.perform(get("/actuator/health")).andExpect(status().is(not(401))).andExpect(status().is(not(403)));
	}

	@ParameterizedTest
	@CsvSource({
			"POST,/api/v1/pokemon/1/sync",
			"DELETE,/api/v1/pokemon/1/sync",
			"PUT,/api/v1/pokemon/1/extras"
	})
	void shouldRejectProtectedRoutesWithoutAToken(String method, String path) throws Exception {
		mockMvc.perform(protectedRequest(method, path))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
	}

	@ParameterizedTest
	@CsvSource({
			"POST,/api/v1/pokemon/1/sync",
			"DELETE,/api/v1/pokemon/1/sync",
			"PUT,/api/v1/pokemon/1/extras"
	})
	void shouldRejectMalformedExpiredAndWrongSignatureTokens(String method, String path) throws Exception {
		mockMvc.perform(protectedRequest(method, path).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(protectedRequest(method, path).header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken()))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(protectedRequest(method, path).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSignedWith("b".repeat(32))))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@CsvSource({
			"POST,/api/v1/pokemon/1/sync",
			"DELETE,/api/v1/pokemon/1/sync",
			"PUT,/api/v1/pokemon/1/extras"
	})
	void shouldAllowProtectedRoutesWithAValidToken(String method, String path) throws Exception {
		String token = validToken();

		mockMvc.perform(protectedRequest(method, path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().is(not(401)))
				.andExpect(status().is(not(403)))
				.andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
	}

	@Test
	void shouldForbidAuthenticatedAccessToUnknownRoutes() throws Exception {
		String token = validToken();

		mockMvc.perform(get("/api/v1/admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Forbidden"));
	}

	@Test
	void shouldDenyUnknownRoutes() throws Exception {
		mockMvc.perform(get("/api/v1/admin"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldAllowCorsPreflightOnlyFromConfiguredOrigins() throws Exception {
		mockMvc.perform(options("/api/v1/pokemon")
						.header(HttpHeaders.ORIGIN, "http://localhost:5173")
						.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

		mockMvc.perform(options("/api/v1/pokemon")
						.header(HttpHeaders.ORIGIN, "http://evil.example")
						.header("Access-Control-Request-Method", "GET"))
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	private static MockHttpServletRequestBuilder protectedRequest(String method, String path) {
		return switch (method) {
			case "POST" -> post(path);
			case "DELETE" -> delete(path);
			case "PUT" -> put(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"localizedName":"Bulbassauro","region":"Kanto","tags":["starter"]}
							""");
			default -> throw new IllegalArgumentException(method);
		};
	}

	private String validToken() {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject("ash")
				.issuedAt(now)
				.expiresAt(now.plus(1, ChronoUnit.HOURS))
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	private String expiredToken() {
		Instant issuedAt = Instant.parse("2020-01-01T00:00:00Z");
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject("ash")
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plus(1, ChronoUnit.HOURS))
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	private static String tokenSignedWith(String secret) {
		NimbusJwtEncoder encoder = new NimbusJwtEncoder(
				new ImmutableSecret<>(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject("ash")
				.issuedAt(now)
				.expiresAt(now.plus(1, ChronoUnit.HOURS))
				.build();
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	private static PokemonDetails details() {
		return new PokemonDetails(
				1,
				"bulbasaur",
				null,
				null,
				"Seed Pokémon",
				new Mass(69),
				List.of(new Ability(65, "overgrow", false)),
				List.of(new Stat("hp", 45)),
				"A strange seed.",
				new EvolutionNode(1, "bulbasaur", null, List.of()),
				1,
				"bulbasaur");
	}

}
