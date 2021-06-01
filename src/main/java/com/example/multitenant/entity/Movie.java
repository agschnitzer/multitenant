package com.example.multitenant.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity @Data
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Long runtime;

    @Column(nullable = false)
    private LocalDate releaseDate;
}
