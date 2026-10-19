package com.fred.pokehub.web.auth;

import com.fred.pokehub.application.port.in.AuthenticateUserUseCase;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.application.port.out.AccessToken;
import com.fred.pokehub.web.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "auth")
public class AuthController {

	private final RegisterUserUseCase registerUser;

	private final AuthenticateUserUseCase authenticateUser;

	public AuthController(RegisterUserUseCase registerUser, AuthenticateUserUseCase authenticateUser) {
		this.registerUser = registerUser;
		this.authenticateUser = authenticateUser;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Register a user")
	@ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = OpenApiConfig.JSON, schema = @Schema(implementation = UserResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request. Validation failures add errors.", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "409", description = "Username already taken", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	UserResponse register(@Valid @RequestBody AuthRequest request) {
		return UserResponse.from(registerUser.register(request.username(), request.password()));
	}

	@PostMapping("/login")
	@Operation(summary = "Log in")
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = OpenApiConfig.JSON, schema = @Schema(implementation = TokenResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request. Validation failures add errors.", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(mediaType = OpenApiConfig.PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
	TokenResponse login(@Valid @RequestBody AuthRequest request) {
		AccessToken token = authenticateUser.authenticate(request.username(), request.password());
		return new TokenResponse(token.token(), "Bearer", token.expiresIn());
	}

}
