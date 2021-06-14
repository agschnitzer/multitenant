package com.example.multitenant.entity;

import com.example.multitenant.config.EncoderConfig;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity @Data
@Builder @AllArgsConstructor @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(min = 6, max = 60)
    private String email;

    @Column(nullable = false)
    private String password;

    @Transient
    private String confirmation;

    @PrePersist
    public void prePersist() {
        password = EncoderConfig.getPasswordEncoder().encode(password);
    }
}
