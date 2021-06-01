package com.example.multitenant.endpoint.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class MovieDto {

    private Long id;
    private String title;
    private Long runtime;
    private LocalDate releaseDate;
}
