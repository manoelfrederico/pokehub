package com.fred.pokehub.web.auth;

import com.fred.pokehub.application.port.in.AuthenticateUserUseCase;
import com.fred.pokehub.application.port.in.RegisterUserUseCase;
import com.fred.pokehub.application.port.out.AccessToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final RegisterUserUseCase registerUser;

	private final AuthenticateUserUseCase authenticateUser;

	public AuthController(RegisterUserUseCase registerUser, AuthenticateUserUseCase authenticateUser) {
		this.registerUser = registerUser;
		this.authenticateUser = authenticateUser;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	UserResponse register(@Valid @RequestBody AuthRequest request) {
		return UserResponse.from(registerUser.register(request.username(), request.password()));
	}

	@PostMapping("/login")
	TokenResponse login(@Valid @RequestBody AuthRequest request) {
		AccessToken token = authenticateUser.authenticate(request.username(), request.password());
		return new TokenResponse(token.token(), "Bearer", token.expiresIn());
	}

}
