package com.fred.pokehub.application.port.in;

import com.fred.pokehub.application.port.out.AccessToken;

/**
 * Verifies credentials and issues an access token.
 */
public interface AuthenticateUserUseCase {

	AccessToken authenticate(String username, String password);

}
