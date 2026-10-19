package com.fred.pokehub.domain.exception;

/**
 * Raised when a page limit or offset is outside the supported range.
 */
public final class InvalidPaginationException extends RuntimeException {

	public InvalidPaginationException(String message) {
		super(message);
	}

}
