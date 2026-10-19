package com.fred.pokehub.application.exception;

/**
 * PokéAPI could not serve a catalog read. The web layer maps this to HTTP 502.
 */
public final class CatalogUnavailableException extends RuntimeException {

	public CatalogUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
