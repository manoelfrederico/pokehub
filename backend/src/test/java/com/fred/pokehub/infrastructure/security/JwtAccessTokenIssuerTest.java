package com.fred.pokehub.infrastructure.security;

import com.fred.pokehub.application.port.out.AccessToken;
import com.fred.pokehub.domain.model.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAccessTokenIssuerTest {

	private static final String SECRET = "a".repeat(32);

	private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);

	private static final Duration TTL = Duration.ofHours(1);

	private static final User ASH = new User(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"ash",
			"hashed");

	private JwtAccessTokenIssuer issuer;

	private JwtDecoder decoder;

	@BeforeEach
	void setUp() {
		SecretKey key = secretKey(SECRET);
		JwtProperties properties = new JwtProperties(SECRET, TTL);
		issuer = new JwtAccessTokenIssuer(
				new NimbusJwtEncoder(new ImmutableSecret<>(key)),
				properties,
				Clock.fixed(NOW, ZoneOffset.UTC));
		decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
	}

	@Test
	void shouldIssueASignedTokenWithSubjectIssuedAtAndExpiry() {
		AccessToken issued = issuer.issue(ASH);

		Jwt jwt = decoder.decode(issued.token());
		assertThat(jwt.getSubject()).isEqualTo("ash");
		assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
		assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(TTL));
		assertThat(issued.expiresIn()).isEqualTo(TTL.toSeconds());
	}

	@Test
	void shouldFailWhenTheTokenIsTampered() {
		String token = issuer.issue(ASH).token();
		String tampered = token.substring(0, token.length() - 2) + "aa";

		assertThatThrownBy(() -> decoder.decode(tampered))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void shouldFailWhenTheSignatureKeyIsWrong() {
		String token = issuer.issue(ASH).token();
		JwtDecoder other = NimbusJwtDecoder.withSecretKey(secretKey("b".repeat(32)))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();

		assertThatThrownBy(() -> other.decode(token))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void shouldRejectANullUser() {
		assertThatThrownBy(() -> issuer.issue(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("user");
	}

	private static SecretKey secretKey(String secret) {
		return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

}
