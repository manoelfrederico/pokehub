package com.fred.pokehub.web.auth;

import com.fred.pokehub.application.exception.InvalidCredentialsException;
import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.application.port.in.AuthenticateUserUseCase;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.application.port.out.AccessToken;
import com.fred.pokehub.config.JsonMapperConfig;
import com.fred.pokehub.domain.exception.FieldViolation;
import com.fred.pokehub.domain.exception.InvalidUserException;
import com.fred.pokehub.domain.model.User;
import com.fred.pokehub.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

	private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private static final String PASSWORD = "Pikachu#2026";

	private FakeRegister register;

	private FakeAuthenticate authenticate;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		register = new FakeRegister();
		authenticate = new FakeAuthenticate();
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(register, authenticate))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.setMessageConverters(new JacksonJsonHttpMessageConverter(jsonMapper()))
				.build();
	}

	@Test
	void shouldRegisterAUser() throws Exception {
		register.result = new User(ID, "ash", "hashed");

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ASH","password":"Pikachu#2026"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(ID.toString()))
				.andExpect(jsonPath("$.username").value("ash"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
		assertThat(register.username).isEqualTo("ASH");
		assertThat(register.password).isEqualTo(PASSWORD);
	}

	@Test
	void shouldLoginAndReturnABearerToken() throws Exception {
		authenticate.token = new AccessToken("jwt-value", 3600);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"Pikachu#2026"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-value"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldRejectABlankUsernameWithFieldErrors() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"","password":"Pikachu#2026"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.errors[0].field").value("username"))
				.andExpect(jsonPath("$.password").doesNotExist());
		assertThat(register.username).isNull();
	}

	@Test
	void shouldRejectAShortPasswordWithFieldErrors() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"short1"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("password"))
				.andExpect(jsonPath("$.errors[0].message").value("must be between 8 and 72 characters"));
		assertThat(authenticate.username).isNull();
	}

	@Test
	void shouldReturnConflictWhenTheUsernameIsTaken() throws Exception {
		register.failure = new UsernameAlreadyTakenException("ash");

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"Pikachu#2026"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Username already taken"))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldReturnTheSameUnauthorizedBodyForUnknownUserAndWrongPassword() throws Exception {
		authenticate.failure = new InvalidCredentialsException();

		MvcResult unknown = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"misty","password":"Pikachu#2026"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.detail").value(InvalidCredentialsException.MESSAGE))
				.andReturn();

		MvcResult wrong = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"WrongPass1"}
								"""))
				.andExpect(status().isUnauthorized())
				.andReturn();

		assertThat(unknown.getResponse().getContentAsString()).isEqualTo(wrong.getResponse().getContentAsString());
		assertThat(unknown.getResponse().getContentAsString()).doesNotContain(PASSWORD);
		assertThat(unknown.getResponse().getContentAsString()).doesNotContain("WrongPass1");
	}

	@Test
	void shouldMapDomainValidationToFieldErrors() throws Exception {
		register.failure = new InvalidUserException(
				new FieldViolation("password", "must contain at least one letter and one digit"));

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"Pikachuuu"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("password"))
				.andExpect(jsonPath("$.errors[0].message").value("must contain at least one letter and one digit"));
	}

	@Test
	void shouldNeverEchoThePassword() throws Exception {
		register.result = new User(ID, "ash", "hashed");

		String body = mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"ash","password":"Pikachu#2026"}
								"""))
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(body).doesNotContain(PASSWORD);
	}

	private static JsonMapper jsonMapper() {
		JsonMapper.Builder builder = JsonMapper.builder();
		JsonMapperConfig.apply(builder);
		return builder.build();
	}

	private static final class FakeRegister implements RegisterUserUseCase {

		private String username;

		private String password;

		private User result;

		private RuntimeException failure;

		@Override
		public User register(String username, String password) {
			this.username = username;
			this.password = password;
			if (failure != null) {
				throw failure;
			}
			return result;
		}

	}

	private static final class FakeAuthenticate implements AuthenticateUserUseCase {

		private String username;

		private AccessToken token;

		private RuntimeException failure;

		@Override
		public AccessToken authenticate(String username, String password) {
			this.username = username;
			if (failure != null) {
				throw failure;
			}
			return token;
		}

	}

}
