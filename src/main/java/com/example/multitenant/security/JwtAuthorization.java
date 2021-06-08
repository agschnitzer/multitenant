package com.example.multitenant.security;

import com.example.multitenant.config.DatabaseConfig;
import com.example.multitenant.config.properties.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthorization extends BasicAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SecurityProperties securityProperties;
    private final DatabaseConfig databaseConfig;

    public JwtAuthorization(AuthenticationManager authenticationManager, SecurityProperties securityProperties,
                            DatabaseConfig databaseConfig) {
        super(authenticationManager);
        this.securityProperties = securityProperties;
        this.databaseConfig = databaseConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            UsernamePasswordAuthenticationToken token = getToken(request);
            SecurityContextHolder.getContext().setAuthentication(token);
        } catch (IllegalArgumentException | JwtException e) {
            LOGGER.debug("Invalid authorization attempt: {}", e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid authorization header or token");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Extracts jwt from http request and sets database context.
     *
     * @param request containing valid jwt.
     * @return an authentication token with specified authorities.
     */
    private UsernamePasswordAuthenticationToken getToken(HttpServletRequest request) {
        String token = request.getHeader(securityProperties.getAuthHeader());

        if (token == null || token.isEmpty() || !token.startsWith(securityProperties.getAuthTokenPrefix())) {
            throw new IllegalArgumentException("Authorization header is malformed or missing");
        }

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(securityProperties.getJwtSecret().getBytes()).build()
                .parseClaimsJws(token.replace(securityProperties.getAuthTokenPrefix(), ""))
                .getBody();

        List<SimpleGrantedAuthority> authorities = ((List<?>) claims
                .get("rol")).stream()
                .map(authority -> new SimpleGrantedAuthority((String) authority))
                .collect(Collectors.toList());

        String username = claims.getSubject();

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Token contains no user");
        }

        // select active database and set thread context accordingly
        if (request.getRequestURI().startsWith("/api/v1/user/")) {
            DatabaseConfig.DBContextHolder.setDefault();
        } else {
            databaseConfig.setActiveDatasource(username);
        }

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
