package org.secassess.core.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.secassess.core.interfaces.JwtService;
import org.secassess.core.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling JWT generation, parsing, and validation to secure API requests.
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtUtils jwtUtils;

    @Value("${app.security.jwt.expiration}")
    private long expirationTime;

    @Override
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return jwtUtils.extractUsername(token);
    }

    @Override
    public String extractRole(String token) {
        return jwtUtils.extractRole(token);
    }

    @Override
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !jwtUtils.isTokenExpired(token));
    }
}