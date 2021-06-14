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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MovieMapperTest implements MovieData {

    @Autowired
    private MovieMapper movieMapper;

    @Test
    @DisplayName("After mapping entity to dto, dto should contain all properties of entity.")
    public void givenEntity_whenMappingToDto_dtoHasAllProperties() {
        Movie entity = getMovie();
        MovieDto dto = movieMapper.movieToMovieDto(entity);

        assertAll(
                () -> assertNull(entity.getId()),
                () -> assertNull(dto.getId()),
                () -> assertEquals(entity.getTitle(), dto.getTitle()),
                () -> assertEquals(entity.getRuntime(), dto.getRuntime()),
                () -> assertEquals(entity.getReleaseDate(), dto.getReleaseDate())
        );
    }

    @Test
    @DisplayName("After mapping dto to entity, entity should contain all properties of dto.")
    public void givenDto_whenMappingToEntity_entityHasAllProperties() {
        MovieDto dto = getMovieDto();
        Movie entity = movieMapper.movieDtoToMovie(dto);

        assertAll(
                () -> assertNull(dto.getId()),
                () -> assertNull(entity.getId()),
                () -> assertEquals(dto.getTitle(), entity.getTitle()),
                () -> assertEquals(dto.getRuntime(), entity.getRuntime()),
                () -> assertEquals(dto.getReleaseDate(), entity.getReleaseDate())
        );
    }

    @Test
    @DisplayName("After mapping null, result should also be null.")
    public void givenNothing_whenMappingToDto_orEntity_resultShouldBeNull() {
        assertNull(movieMapper.movieToMovieDto(null));
        assertNull(movieMapper.movieDtoToMovie(null));
    }
}
