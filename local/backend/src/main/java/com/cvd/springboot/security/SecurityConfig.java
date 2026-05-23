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

                // ── Fully public (auth endpoints) ─────────────────
                .requestMatchers("/api/auth/**").permitAll()

                // ── Researcher dashboard — intentionally public ───
                .requestMatchers("/api/researcher/**").permitAll()

                // ── Patient self-registration & lookup ────────────
                // POST /api/patients  — create account (pre-login)
                .requestMatchers(HttpMethod.POST, "/api/patients").permitAll()
                // GET  /api/patients/phone/** — fetch own record by phone (pre-login)
                .requestMatchers(HttpMethod.GET,  "/api/patients/phone/**").permitAll()

                // ── Everything else requires a valid JWT ──────────
                // This covers:
                //   /api/predictions/**   — run/view/delete predictions
                //   /api/patients/**      — admin patient management, PUT updates
                //   /api/admin/**         — admin management
                //   /api/doctors/**       — doctor routes
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
