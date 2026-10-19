package com.Unimag.PulsePass.security;

import com.Unimag.PulsePass.dto.response.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        if (properties.getSecretBase64() == null || properties.getSecretBase64().isBlank())
            throw new IllegalStateException("PULSEPASS_JWT_SECRET_BASE64 must be configured.");
        try {
            byte[] bytes = Decoders.BASE64.decode(properties.getSecretBase64());
            if (bytes.length < 32) throw new IllegalStateException("JWT secret must contain at least 32 random bytes.");
            this.key = Keys.hmacShaKeyFor(bytes);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT secret must be valid Base64 with at least 32 bytes.", ex);
        }
        if (properties.getIssuer() == null || properties.getIssuer().isBlank()
                || properties.getAccessTokenTtl() == null
                || properties.getAccessTokenTtl().isZero() || properties.getAccessTokenTtl().isNegative())
            throw new IllegalStateException("JWT issuer and positive access token TTL are required.");
    }

    public AuthResponse issueToken(SecurityPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getAccessTokenTtl());
        String token = Jwts.builder()
                .subject(principal.getUserId().toString())
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new AuthResponse(token, "Bearer", properties.getAccessTokenTtl().toSeconds());
    }

    public Long parseUserId(String token) {
        var parsed = Jwts.parser().verifyWith(key).requireIssuer(properties.getIssuer()).build()
                .parseSignedClaims(token);
        if (!"HS256".equals(parsed.getHeader().getAlgorithm()))
            throw new JwtException("Unexpected token signing algorithm.");
        Claims claims = parsed.getPayload();
        Date now = new Date();
        if (claims.getSubject() == null || claims.getSubject().isBlank() || claims.getId() == null
                || claims.getIssuedAt() == null || claims.getExpiration() == null
                || claims.getIssuedAt().after(now) || !claims.getExpiration().after(now))
            throw new JwtException("Required token claims are missing or invalid.");
        try { return Long.valueOf(claims.getSubject()); }
        catch (NumberFormatException ex) { throw new JwtException("Invalid token subject.", ex); }
    }
}
