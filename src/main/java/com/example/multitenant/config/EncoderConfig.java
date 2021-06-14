package com.example.multitenant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /**
     * Gets new password encoder to use in entities.
     *
     * @return password encoder.
     */
    public static PasswordEncoder getPasswordEncoder() { return new BCryptPasswordEncoder(); }
}
