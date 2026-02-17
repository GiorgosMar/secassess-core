package org.secassess.core.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.secassess.core.enums.UserRole;
import org.secassess.core.filters.CorrelationIdFilter;
import org.secassess.core.filters.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorrelationIdFilter correlationIdFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF as we use JWT (Stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Set session management to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Custom error handling for 401 and 403
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // Endpoint authorization configuration
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/swagger-ui/**"
                        ).permitAll()

                        // Secured endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/organizations/**")
                        .hasRole(UserRole.ADMIN.name())

                        .requestMatchers(HttpMethod.PATCH, "/api/v1/templates/*/publish")
                        .hasRole(UserRole.ADMIN.name())

                        // Using AUDITOR here (instead of ASSESSOR)
                        .requestMatchers("/api/v1/assessments/*/copy-from-template")
                        .hasAnyRole(UserRole.AUDITOR.name(), UserRole.ADMIN.name())

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                // Filter ordering: CorrelationID -> JWT -> UsernamePassword
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(correlationIdFilter, JwtAuthenticationFilter.class)

                .build();
    }

    /**
     * Handles 401 Unauthorized errors (Not logged in)
     */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
        };
    }

    /**
     * Handles 403 Forbidden errors (Logged in but not enough permissions)
     */
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied\"}");
        };
    }
}