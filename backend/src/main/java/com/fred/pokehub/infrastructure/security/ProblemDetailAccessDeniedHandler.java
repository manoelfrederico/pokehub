package com.fred.pokehub.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

final class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

	private final JsonMapper mapper;

	ProblemDetailAccessDeniedHandler(JsonMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
			throws IOException {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access is denied");
		problem.setTitle("Forbidden");
		ProblemDetails.write(mapper, response, problem);
	}

}
