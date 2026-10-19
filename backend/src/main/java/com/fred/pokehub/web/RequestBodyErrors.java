package com.fred.pokehub.web;

import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Turns a failed JSON read into one field error. Messages never repeat the submitted value.
 */
final class RequestBodyErrors {

	private RequestBodyErrors() {
	}

	static FieldErrorResponse from(HttpMessageNotReadableException exception) {
		Throwable cause = mappingCause(exception);
		if (cause instanceof UnrecognizedPropertyException unrecognized) {
			return new FieldErrorResponse(unrecognized.getPropertyName(), "is not allowed");
		}
		if (cause instanceof MismatchedInputException mismatched) {
			String field = fieldName(mismatched);
			if (field == null) {
				return new FieldErrorResponse("body", "must be a JSON object");
			}
			return new FieldErrorResponse(field, "has an invalid type");
		}
		if (missingBody(exception)) {
			return new FieldErrorResponse("body", "is required");
		}
		return new FieldErrorResponse("body", "is malformed");
	}

	private static Throwable mappingCause(Throwable throwable) {
		Throwable current = throwable;
		MismatchedInputException mismatched = null;
		while (current != null) {
			if (current instanceof UnrecognizedPropertyException unrecognized) {
				return unrecognized;
			}
			if (mismatched == null && current instanceof MismatchedInputException found) {
				mismatched = found;
			}
			current = current.getCause();
		}
		return mismatched;
	}

	private static String fieldName(MismatchedInputException exception) {
		String name = null;
		for (JacksonException.Reference reference : exception.getPath()) {
			if (reference.getPropertyName() != null) {
				name = reference.getPropertyName();
			}
		}
		return name;
	}

	private static boolean missingBody(HttpMessageNotReadableException exception) {
		String message = exception.getMessage();
		return message != null && message.startsWith("Required request body is missing");
	}

}
