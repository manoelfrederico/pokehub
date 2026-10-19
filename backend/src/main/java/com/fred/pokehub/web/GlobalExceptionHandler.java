package com.fred.pokehub.web;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.exception.InvalidCredentialsException;
import com.fred.pokehub.application.exception.PokemonNotFoundException;
import com.fred.pokehub.application.exception.PokemonNotSyncedException;
import com.fred.pokehub.application.exception.UsernameAlreadyTakenException;
import com.fred.pokehub.domain.exception.InvalidPaginationException;
import com.fred.pokehub.domain.exception.InvalidPokemonExtrasException;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import com.fred.pokehub.domain.exception.InvalidUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(InvalidPaginationException.class)
	ProblemDetail handleInvalidPagination(InvalidPaginationException exception) {
		return badRequest(exception);
	}

	@ExceptionHandler(InvalidPokemonIdException.class)
	ProblemDetail handleInvalidPokemonId(InvalidPokemonIdException exception) {
		return badRequest(exception);
	}

	@ExceptionHandler(PokemonNotFoundException.class)
	ProblemDetail handlePokemonNotFound(PokemonNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setTitle("Pokémon not found");
		return problem;
	}

	@ExceptionHandler(PokemonNotSyncedException.class)
	ProblemDetail handlePokemonNotSynced(PokemonNotSyncedException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setTitle("Pokémon not stored");
		return problem;
	}

	@ExceptionHandler(InvalidPokemonExtrasException.class)
	ProblemDetail handleInvalidPokemonExtras(InvalidPokemonExtrasException exception) {
		List<FieldErrorResponse> errors = exception.violations().stream()
				.map(violation -> new FieldErrorResponse(violation.field(), violation.message()))
				.toList();
		return invalidBody(errors);
	}

	@ExceptionHandler(InvalidUserException.class)
	ProblemDetail handleInvalidUser(InvalidUserException exception) {
		List<FieldErrorResponse> errors = exception.violations().stream()
				.map(violation -> new FieldErrorResponse(violation.field(), violation.message()))
				.toList();
		return invalidBody(errors);
	}

	@ExceptionHandler(UsernameAlreadyTakenException.class)
	ProblemDetail handleUsernameAlreadyTaken(UsernameAlreadyTakenException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("Username already taken");
		return problem;
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
		problem.setTitle("Unauthorized");
		return problem;
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		ProblemDetail problem = invalidBody(List.of(RequestBodyErrors.from(exception)));
		return handleExceptionInternal(exception, problem, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		List<FieldErrorResponse> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
				.toList();
		return handleExceptionInternal(exception, invalidBody(errors), headers, status, request);
	}

	@ExceptionHandler(CatalogUnavailableException.class)
	ProblemDetail handleCatalogUnavailable(CatalogUnavailableException exception) {
		log.warn("Pokemon catalog is unavailable", exception);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_GATEWAY,
				"The Pokémon catalog is temporarily unavailable.");
		problem.setTitle("Catalog unavailable");
		return problem;
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception exception) {
		log.error("Unexpected error", exception);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
		problem.setTitle("Internal error");
		return problem;
	}

	private static ProblemDetail badRequest(RuntimeException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problem.setTitle("Bad request");
		return problem;
	}

	private static ProblemDetail invalidBody(List<FieldErrorResponse> errors) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request body is invalid.");
		problem.setTitle("Bad request");
		problem.setProperty("errors", errors);
		return problem;
	}

}
