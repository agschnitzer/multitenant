package com.example.multitenant.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity @Data
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
}
