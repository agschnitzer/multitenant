package com.example.multitenant.security;

import com.example.multitenant.config.properties.SecurityProperties;
import com.example.multitenant.endpoint.dto.UserAuthenticationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

public class JwtAuthentication extends UsernamePasswordAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer tokenizer;

    public JwtAuthentication(SecurityProperties securityProperties, AuthenticationManager authenticationManager,
                             JwtTokenizer tokenizer) {
        setFilterProcessesUrl(securityProperties.getLoginUri());

        this.authenticationManager = authenticationManager;
        this.tokenizer = tokenizer;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            UserAuthenticationDto user = new ObjectMapper()
                    .readValue(request.getInputStream(), UserAuthenticationDto.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
        } catch (IOException e) {
            throw new BadCredentialsException("Wrong API request or JSON schema", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException {
        User user = ((User) authResult.getPrincipal());
        response.getWriter().write(
                tokenizer.createToken(user.getUsername(), user.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
                )
        );

        LOGGER.info("Successfully authenticated user {}", user.getUsername());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(failed.getMessage());
        LOGGER.debug("Invalid authentication attempt: {}", failed.getMessage());
    }
}
