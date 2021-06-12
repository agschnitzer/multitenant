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

    Long ID = 1L;
    String TITLE = "Movie Title";
    Long RUNTIME = 123L;
    LocalDate RELEASE_DATE = LocalDate.of(2020, Month.JANUARY, 1);

    /**
     * Builds a movie entity.
     *
     * @return entity containing details about movie.
     */
    default Movie getMovie() {
        return Movie.builder()
                .title(TITLE)
                .runtime(RUNTIME)
                .releaseDate(RELEASE_DATE)
                .build();
    }

    /**
     * Builds a movie dto.
     *
     * @return dto containing details about movie.
     */
    default MovieDto getMovieDto() {
        return MovieDto.builder()
                .title(TITLE)
                .runtime(RUNTIME)
                .releaseDate(RELEASE_DATE)
                .build();
    }

    /**
     * Builds a movie entity.
     *
     * @return json object containing details about movie.
     * @throws JsonProcessingException if something goes wrong during parsing json.
     */
    default String getMovieDtoJson() throws JsonProcessingException {
        return getMovieMapper().writeValueAsString(getMovie());
    }

    /**
     * Builds a movie dto;
     *
     * @param json object containing details about movie.
     * @return dto containing all properties of object.
     * @throws JsonProcessingException if something goes wrong during parsing json.
     */
    default MovieDto getMovieDto(String json) throws JsonProcessingException {
        return getMovieMapper().readValue(json, MovieDto.class);
    }

    /**
     * Builds an object mapper.
     *
     * @return object mapper with support of LocalDate.
     */
    default ObjectMapper getMovieMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
