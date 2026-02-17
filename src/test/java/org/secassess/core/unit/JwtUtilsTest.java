package org.secassess.core.unit;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.secassess.core.utils.JwtUtils;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private final String testSecret = Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());

    @BeforeEach
    void setUp() {
        MDC.put("correlationId", "UNIT-TEST-UTILS-" + UUID.randomUUID().toString().substring(0, 5));
        log.info("Initializing JwtUtils for testing...");

        jwtUtils = new JwtUtils();
        // Εγχέουμε το secretKey στην @Value μεταβλητή
        ReflectionTestUtils.setField(jwtUtils, "secretKey", testSecret);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should extract username correctly from a real token")
    void extractUsername_ShouldReturnCorrectSubject() {
        log.info("STEP 1: Generating a real token for testing");
        String expectedUsername = "security_expert";
        String token = createTestToken(expectedUsername, "ADMIN");

        log.info("STEP 2: Extracting username using JwtUtils");
        String actualUsername = jwtUtils.extractUsername(token);

        log.info("STEP 3: Asserting username matches");
        assertEquals(expectedUsername, actualUsername);
        log.info("Username extraction test passed!");
    }

    @Test
    @DisplayName("Should correctly identify if a token has expired")
    void isTokenExpired_ShouldReturnFalseForNewToken() {
        log.info("STEP 1: Creating a fresh token");
        String token = createTestToken("user", "USER");

        log.info("STEP 2: Checking expiration status");
        boolean expired = jwtUtils.isTokenExpired(token);

        log.info("STEP 3: Asserting token is NOT expired");
        assertFalse(expired);
    }

    @Test
    @DisplayName("Should extract role from claims correctly")
    void extractRole_ShouldReturnAdmin() {
        log.info("STEP 1: Creating token with role ADMIN");
        String token = createTestToken("admin_user", "ADMIN");

        log.info("STEP 2: Extracting role claim");
        String role = jwtUtils.extractRole(token);

        log.info("STEP 3: Asserting role is ADMIN");
        assertEquals("ADMIN", role);
    }

    private String createTestToken(String subject, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}