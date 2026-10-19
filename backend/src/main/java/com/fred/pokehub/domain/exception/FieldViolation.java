package com.fred.pokehub.domain.exception;

/**
 * One invalid field. {@code message} describes the rule and does not repeat the submitted value.
 */
public record FieldViolation(String field, String message) {

	public FieldViolation {
		if (field == null || field.isBlank()) {
			throw new IllegalArgumentException("field must not be blank");
		}
		if (message == null || message.isBlank()) {
			throw new IllegalArgumentException("message must not be blank");
		}
	}

}
