package com.example.multitenant.endpoint.mapper;

import com.example.multitenant.endpoint.dto.MovieDto;
import com.example.multitenant.entity.Movie;
import org.mapstruct.Mapper;

@Mapper
public interface MovieMapper {

    /**
     * Maps a movie entity to a movie dto.
     *
     * @param movie entity.
     * @return a dto containing all movie details.
     */
    MovieDto movieToMovieDto(Movie movie);

    /**
     * Maps a movie dto to a movie entity.
     *
     * @param movieDto dto.
     * @return an entity containing all movie details.
     */
    Movie movieDtoToMovie(MovieDto movieDto);
}
