package com.fred.pokehub.web.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
