package com.example.multitenant.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SecurityProperties {

    @Autowired
    private Auth auth;

    @Autowired
    private Jwt jwt;

    public String getAuthHeader() {
        return auth.header;
    }

    public String getAuthTokenPrefix() {
        return auth.prefix;
    }

    public String getLoginUri() {
        return auth.loginUri;
    }

    public List<String> getWhitelist() {
        return auth.whitelist;
    }

    public String getJwtSecret() {
        return jwt.secret;
    }

    public String getJwtType() {
        return jwt.type;
    }

    public String getJwtIssuer() {
        return jwt.issuer;
    }

    public String getJwtAudience() {
        return jwt.audience;
    }

    public Long getJwtExpirationTime() {
        return jwt.expirationTime;
    }

    @Bean
    @ConfigurationProperties("security.auth")
    protected Auth auth() { return new Auth(); }

    @Bean
    @ConfigurationProperties("security.jwt")
    protected Jwt jwt() { return new Jwt(); }

    @Getter @Setter
    protected static class Auth {

        private String header;
        private String prefix;
        private String loginUri;
        private List<String> whitelist;
    }

    @Getter @Setter
    protected static class Jwt {

        private String secret;
        private String type;
        private String issuer;
        private String audience;
        private Long expirationTime;
    }
}
