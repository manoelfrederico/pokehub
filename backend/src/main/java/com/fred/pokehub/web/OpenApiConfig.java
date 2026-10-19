package com.fred.pokehub.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(info = @Info(title = "Pokehub", version = "v1"))
@SecurityScheme(
		name = OpenApiConfig.BEARER,
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT",
		description = "JWT access token from /auth/login")
public final class OpenApiConfig {

	public static final String BEARER = "bearer";

	public static final String PROBLEM = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

	public static final String JSON = MediaType.APPLICATION_JSON_VALUE;

}
