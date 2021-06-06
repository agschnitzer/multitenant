package com.example.multitenant.data;

import com.example.multitenant.entity.Movie;

import java.time.LocalDate;
import java.time.Month;

public interface MovieData {

    String TITLE = "Movie Title";
    Long RUNTIME = 123L;
    LocalDate RELEASE_DATE = LocalDate.of(2020, Month.JANUARY, 1);

    /**
     * Builds a movie entity.
     *
     * @return movie entity containing title, runtime and release date.
     */
    default Movie getMovie() {
        return Movie.builder()
                .title(TITLE)
                .runtime(RUNTIME)
                .releaseDate(RELEASE_DATE)
                .build();
    }
}
