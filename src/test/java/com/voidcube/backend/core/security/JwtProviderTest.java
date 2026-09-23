package com.voidcube.backend.core.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String secret = "test-secret-key-voidcube-must-be-at-least-256-bits-long-and-secure!";
    private final String issuer = "http://test.voidcube.com";
    private final long expirySeconds = 3600;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(secret, issuer, expirySeconds);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido com claims customizadas de usuário e empresa")
    void generateAccessToken_WithCustomClaims_GeneratesValidToken() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        String email = "colaborador@voidcube.com";

        String token = jwtProvider.generateAccessToken(userId, email, companyId, true, "SERVICE_USER");

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo(email);

        Claims claims = jwtProvider.getClaims(token);
        assertThat(claims.getIssuer()).isEqualTo(issuer);
        assertThat(claims.get("temporary_password", Boolean.class)).isTrue();
        assertThat(claims.get("user_type", String.class)).isEqualTo("SERVICE_USER");
        assertThat(claims.get("company_id", String.class)).isEqualTo(companyId.toString());
    }

    @Test
    @DisplayName("Deve rejeitar token quando o conteúdo for alterado ou adulterado")
    void validateToken_ReturnsFalse_WhenTokenIsTampered() {
        UUID userId = UUID.randomUUID();
        String token = jwtProvider.generateAccessToken(userId, "test@voidcube.com");

        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertThat(jwtProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false para token mal formatado ou nulo")
    void validateToken_ReturnsFalse_WhenMalformed() {
        assertThat(jwtProvider.validateToken("not-a-token")).isFalse();
        assertThat(jwtProvider.validateToken(null)).isFalse();
        assertThat(jwtProvider.validateToken("")).isFalse();
    }
}