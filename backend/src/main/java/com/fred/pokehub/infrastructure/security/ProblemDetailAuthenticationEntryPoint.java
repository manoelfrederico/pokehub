package com.fred.pokehub.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

final class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final JsonMapper mapper;

	ProblemDetailAuthenticationEntryPoint(JsonMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
			throws IOException {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
		problem.setTitle("Unauthorized");
		ProblemDetails.write(mapper, response, problem);
	}

}
