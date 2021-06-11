package com.example.multitenant.unit.mapper;

import com.example.multitenant.data.MovieData;
import com.example.multitenant.endpoint.dto.MovieDto;
import com.example.multitenant.endpoint.mapper.MovieMapper;
import com.example.multitenant.entity.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MovieMapperTest implements MovieData {

    @Autowired
    private MovieMapper movieMapper;

    @Test
    @DisplayName("After mapping Movie to MovieDto, dto should contain same properties as entity.")
    public void givenMovie_whenMappingToMovieDto_dtoHasAllProperties() {
        Movie entity = getMovie();
        MovieDto dto = movieMapper.movieToMovieDto(entity);

        assertAll(
                () -> assertEquals(entity.getTitle(), dto.getTitle()),
                () -> assertEquals(entity.getRuntime(), dto.getRuntime()),
                () -> assertEquals(entity.getReleaseDate(), dto.getReleaseDate())
        );
    }

    @Test
    @DisplayName("After mapping MovieDto to Movie, entity should contain same properties as dto.")
    public void givenMovieDto_whenMappingToMovie_entityHasAllProperties() {
        MovieDto dto = getMovieDto();
        Movie entity = movieMapper.movieDtoToMovie(dto);

        assertAll(
                () -> assertEquals(dto.getTitle(), entity.getTitle()),
                () -> assertEquals(dto.getRuntime(), entity.getRuntime()),
                () -> assertEquals(dto.getReleaseDate(), entity.getReleaseDate())
        );
    }

    @Test
    @DisplayName("After mapping empty object, result should also be empty.")
    public void givenNothing_whenMappingToMovieDto_orMovie_resultShouldBeNull() {
        assertNull(movieMapper.movieToMovieDto(null));
        assertNull(movieMapper.movieDtoToMovie(null));
    }
}
