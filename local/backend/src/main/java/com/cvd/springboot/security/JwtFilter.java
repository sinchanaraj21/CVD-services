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
        String path = request.getRequestURI();
        
        // Debug logging
        System.out.println("🔍 JWT Filter - Path: " + path);
        
        // Skip filter for these paths
        boolean skip = path.startsWith("/api/auth") ||
                       path.startsWith("/api/patients") ||
                       path.startsWith("/api/predictions");
        
        System.out.println("   Should skip JWT check: " + skip);
        
        return skip;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        System.out.println("⚠️ JWT Filter - doFilterInternal called for: " + request.getRequestURI());
        
        String header = request.getHeader("Authorization");
        
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("❌ JWT Filter - No valid Authorization header");
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
            
            System.out.println("✅ JWT Filter - Token validated");
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            System.out.println("❌ JWT Filter - Token validation failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or expired token: " + e.getMessage() + "\"}");
        }
    }
}