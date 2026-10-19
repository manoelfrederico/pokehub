package com.fred.pokehub.application.port.out;

import com.fred.pokehub.domain.model.User;

/**
 * Issues a short-lived access token for an authenticated user.
 */
public interface AccessTokenIssuer {

	AccessToken issue(User user);

}
