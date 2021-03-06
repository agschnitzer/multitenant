package com.example.multitenant.unit.service;

import com.example.multitenant.config.DatabaseConfig;
import com.example.multitenant.data.MovieData;
import com.example.multitenant.data.UserData;
import com.example.multitenant.entity.Movie;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;
import com.example.multitenant.repository.MovieRepository;
import com.example.multitenant.service.MovieService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MovieServiceTest implements MovieData, UserData {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private DatabaseConfig databaseConfig;

    @BeforeEach
    public void beforeEach() {
        databaseConfig.setActiveDatasource(EMAIL);
    }

    @AfterEach
    public void afterEach() {
        movieRepository.deleteAll();
    }

    @Test
    @DisplayName("Getting stored entity should return entity.")
    public void storedEntity_whenGettingEntity_shouldReturnEntity() {
        Movie stored = getMovie();
        movieRepository.save(stored);

        Movie returned = movieService.findById(stored.getId());

        assertAll(
                () -> assertNotNull(returned),
                () -> assertEquals(stored, returned)
        );
    }

    @Test
    @DisplayName("Trying to get a non-stored entity should throw an exception.")
    public void storedNothing_whenGettingEntity_shouldThrowException() {
        assertThrows(NotFoundException.class, () -> movieService.findById(ID));
    }

    @Test
    @DisplayName("Saving entity should return its id.")
    public void storedNothing_orOtherEntities_whenSavingEntity_shouldReturnId() {
        Movie stored = getMovie();
        Long id1 = movieService.save(stored);

        assertAll(
                () -> assertNotNull(id1),
                () -> assertEquals(stored.getId(), id1)
        );

        Long id2 = movieService.save(stored);

        assertAll(
                () -> assertNotNull(id2),
                () -> assertEquals(stored.getId(), id2)
        );
    }
}
