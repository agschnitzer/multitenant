package com.example.multitenant.security;

import com.example.multitenant.config.properties.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtTokenizer {

    private final SecurityProperties securityProperties;

    @Autowired
    public JwtTokenizer(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Creates user specific jwt.
     *
     * @param username identifier of user.
     * @param roles user specific permissions.
     * @return a string representing a valid jwt.
     */
    public String createToken(String username, List<String> roles) {
        return securityProperties.getAuthTokenPrefix() + Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(securityProperties.getJwtSecret().getBytes()), SignatureAlgorithm.HS512)
                .setHeaderParam("typ", securityProperties.getJwtType())
                .setIssuer(securityProperties.getJwtIssuer())
                .setAudience(securityProperties.getJwtAudience())
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + securityProperties.getJwtExpirationTime()))
                .claim("rol", roles)
                .compact();
    }
}
