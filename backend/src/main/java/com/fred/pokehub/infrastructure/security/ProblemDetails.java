package com.fred.pokehub.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ProblemDetails {

	private ProblemDetails() {
	}

	static void write(JsonMapper mapper, HttpServletResponse response, ProblemDetail problem) throws IOException {
		HttpStatus status = HttpStatus.valueOf(problem.getStatus());
		response.setStatus(status.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		mapper.writeValue(response.getOutputStream(), problem);
	}

}
