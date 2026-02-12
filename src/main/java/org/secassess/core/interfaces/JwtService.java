package org.secassess.core.interfaces;

/**
 * Interface defining the contract for JWT operations including generation,
 * extraction of claims, and token validation.
 */
public interface JwtService {
    String generateToken(String username, String role);
    String extractUsername(String token);
    String extractRole(String token);
    boolean isTokenValid(String token, String username);
}