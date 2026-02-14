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
            // Disable CSRF for API usage
            .csrf(csrf -> csrf.disable())

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints open
                .requestMatchers("/api/auth/**").permitAll()

                // Allow patient creation without auth (current logic)
                .requestMatchers(HttpMethod.POST, "/api/patients").permitAll()

                // Allow predictions (fix for 403)
                .requestMatchers("/api/predictions/**").permitAll()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // Add JWT filter before default auth filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
