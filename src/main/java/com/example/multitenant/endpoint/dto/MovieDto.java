package com.example.multitenant.endpoint.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @ToString
@Builder @AllArgsConstructor @NoArgsConstructor
public class MovieDto {

    private Long id;
    private String title;
    private Long runtime;
    private LocalDate releaseDate;
}
