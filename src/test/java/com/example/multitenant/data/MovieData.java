package com.example.multitenant.data;

import com.example.multitenant.endpoint.dto.MovieDto;
import com.example.multitenant.entity.Movie;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDate;
import java.time.Month;

public interface MovieData {

    String TITLE = "Movie Title";
    Long RUNTIME = 123L;
    LocalDate RELEASE_DATE = LocalDate.of(2020, Month.JANUARY, 1);

    /**
     * Builds a movie entity.
     *
     * @return movie entity containing all details.
     */
    default Movie getMovie() {
        return Movie.builder()
                .title(TITLE)
                .runtime(RUNTIME)
                .releaseDate(RELEASE_DATE)
                .build();
    }

    /**
     * Bulds a movie dto.
     *
     * @return movie dto containing all details.
     */
    default MovieDto getMovieDto() {
        return MovieDto.builder()
                .title(TITLE)
                .runtime(RUNTIME)
                .releaseDate(RELEASE_DATE)
                .build();
    }

    /**
     *  Builds a movie entity.
     *
     * @return movie entity in json format.
     */
    default String getMovieJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper.writeValueAsString(getMovie());
    }
}
