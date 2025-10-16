package com.bank.bank_app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = Logger.getLogger(JwtAuthFilter.class.getName());

    private final com.bank.bank_app.service.JwtService jwtService;
    private final com.bank.bank_app.service.SecurityService securityService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/api/auth/**",
            "/api-docs.yaml"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public JwtAuthFilter(com.bank.bank_app.service.JwtService jwtService,
                         com.bank.bank_app.service.SecurityService securityService) {
        this.jwtService = jwtService;
        this.securityService = securityService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        for (String pattern : PUBLIC_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (Objects.nonNull(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = securityService.loadUserByUsername(username);
                    boolean valid = jwtService.isTokenValid(token, userDetails);

                    if (valid) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.log(Level.FINE, "JWT authenticated for user: " + username);
                    } else {
                        log.log(Level.FINE, "JWT token invalid for user: " + username);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to process JWT authentication", ex);
        }

        filterChain.doFilter(request, response);
    }
}