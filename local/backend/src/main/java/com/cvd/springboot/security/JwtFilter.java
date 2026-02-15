package com.cvd.springboot.security;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final SecretKey key = Keys.hmacShaKeyFor(
        "cvd_secret_key_for_jwt_token_generation_123456".getBytes()
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path   = request.getRequestURI();
        String method = request.getMethod();

        // Public – no token needed
        if (path.startsWith("/api/auth"))               return true;  // login / OTP
        if (path.startsWith("/api/predictions"))        return true;  // patient predictions
        if ("POST".equals(method) && path.equals("/api/patients")) return true; // patient create

        // Admin routes MUST go through the filter so we can validate the JWT
        // Everything else also goes through the filter
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
            return;
        }

        String token = header.substring(7);

        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

            // Token valid — set a simple authentication so Spring Security is satisfied
            org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "authenticated", null,
                        java.util.Collections.emptyList()
                    )
                );

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
        }
    }
}
