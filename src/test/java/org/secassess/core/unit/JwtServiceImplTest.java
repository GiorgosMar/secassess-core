package org.secassess.core.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secassess.core.service.JwtServiceImpl;
import org.secassess.core.utils.JwtUtils;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private final String username = "testUser";
    private final String role = "ADMIN";
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private Key signingKey;

    @BeforeEach
    void setUp() {
        // Set a manual TraceID for the test logs
        MDC.put("correlationId", "UNIT-TEST-" + java.util.UUID.randomUUID().toString().substring(0, 8));

        log.info("Setting up JwtServiceImplTest...");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 3600000L);

        byte[] keyBytes = Base64.getDecoder().decode(secret);
        signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate a token and call JwtUtils for signing key")
    void generateToken_ShouldReturnValidString() {
        log.info("STEP 1: Arranging mocks for token generation");
        when(jwtUtils.getSigningKey()).thenReturn(signingKey);

        log.info("STEP 2: Acting - Calling generateToken for user: {}", username);
        String token = jwtService.generateToken(username, role);

        log.info("STEP 3: Asserting results. Token length: {}", token.length());
        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(jwtUtils, times(1)).getSigningKey();
        log.info("Test passed: generateToken_ShouldReturnValidString");
    }

    @Test
    @DisplayName("Should validate token correctly when username matches and not expired")
    void isTokenValid_ShouldReturnTrue() {
        log.info("STEP 1: Arranging token validation scenario (Valid Case)");
        String token = "mocked.jwt.token";
        when(jwtUtils.extractUsername(token)).thenReturn(username);
        when(jwtUtils.isTokenExpired(token)).thenReturn(false);

        log.info("STEP 2: Acting - Validating token for user: {}", username);
        boolean isValid = jwtService.isTokenValid(token, username);

        log.info("STEP 3: Asserting that token is valid");
        assertTrue(isValid);
        log.info("Test passed: isTokenValid_ShouldReturnTrue");
    }

    @Test
    @DisplayName("Should return false when username does not match")
    void isTokenValid_ShouldReturnFalse_WhenUsernameDiffers() {
        log.info("STEP 1: Arranging token validation scenario (Mismatching User)");
        String token = "mocked.jwt.token";
        when(jwtUtils.extractUsername(token)).thenReturn("wrongUser");

        log.info("STEP 2: Acting - Validating token for user: {} against extracted: wrongUser", username);
        boolean isValid = jwtService.isTokenValid(token, username);

        log.info("STEP 3: Asserting that token is invalid");
        assertFalse(isValid);
        log.info("Test passed: isTokenValid_ShouldReturnFalse_WhenUsernameDiffers");
    }
}