package com.fred.pokehub.web;

/**
 * One invalid request field. {@code message} does not repeat the submitted value.
 */
public record FieldErrorResponse(String field, String message) {
}
