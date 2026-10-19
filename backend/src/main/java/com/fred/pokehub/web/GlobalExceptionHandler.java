package com.fred.pokehub.web;

import com.fred.pokehub.application.exception.CatalogUnavailableException;
import com.fred.pokehub.application.exception.PokemonNotFoundException;
import com.fred.pokehub.application.exception.PokemonNotSyncedException;
import com.fred.pokehub.domain.exception.InvalidPaginationException;
import com.fred.pokehub.domain.exception.InvalidPokemonIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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

}
