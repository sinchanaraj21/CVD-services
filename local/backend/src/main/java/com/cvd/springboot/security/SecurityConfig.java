package com.cvd.springboot.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // ── Fully public ──────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/predictions/**").permitAll()
                .requestMatchers(HttpMethod.POST,   "/api/patients").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/patients/phone/**").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/patients/{patientId}").permitAll()
                .requestMatchers(HttpMethod.PUT,    "/api/patients/**").permitAll()

                // ── Admin routes — require valid JWT ──────────────
                .requestMatchers("/api/patients/admin/**").authenticated()

                // ── Everything else — require auth ────────────────
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
