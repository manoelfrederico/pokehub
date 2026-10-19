package com.fred.pokehub.infrastructure.security;

import com.fred.pokehub.application.port.out.AccessToken;
import com.fred.pokehub.application.port.out.AccessTokenIssuer;
import com.fred.pokehub.domain.model.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

	private final JwtEncoder encoder;

	private final JwtProperties properties;

	private final Clock clock;

	public JwtAccessTokenIssuer(JwtEncoder encoder, JwtProperties properties, Clock clock) {
		this.encoder = Objects.requireNonNull(encoder, "encoder");
		this.properties = Objects.requireNonNull(properties, "properties");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public AccessToken issue(User user) {
		Objects.requireNonNull(user, "user");
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(properties.ttl());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(user.username())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new AccessToken(token, properties.ttl().toSeconds());
	}

}
