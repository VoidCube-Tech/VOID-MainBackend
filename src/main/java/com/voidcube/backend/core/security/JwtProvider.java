package com.voidcube.backend.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final String issuer;
    private final long accessTokenExpirationSeconds;

    public JwtProvider(
            @Value("${security.jwt.secret:default-secret-key-voidcube-must-be-at-least-256-bits-long}") String secret,
            @Value("${security.jwt.issuer:http://localhost:8080}") String issuer,
            @Value("${security.jwt.access-token-expiry-seconds:900}") long accessTokenExpirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    public String generateAccessToken(UUID userId, String email) {
        return generateAccessToken(userId, email, null, false, "PLATFORM");
    }

    public String generateAccessToken(UUID userId, String email, UUID companyId, boolean temporaryPassword, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (accessTokenExpirationSeconds * 1000));

        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("temporary_password", temporaryPassword)
                .claim("user_type", userType)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate);

        if (companyId != null) {
            builder.claim("company_id", companyId.toString());
        }

        return builder.signWith(key).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}