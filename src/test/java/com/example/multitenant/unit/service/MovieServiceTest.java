package com.example.multitenant.unit.service;

import com.example.multitenant.data.MovieData;
import com.example.multitenant.entity.Movie;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;
import com.example.multitenant.repository.MovieRepository;
import com.example.multitenant.service.MovieService;
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
public class MovieServiceTest implements MovieData {

    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieRepository movieRepository;

    @Test
    @DisplayName("After trying to get stored movie, should return movie.")
    public void storedMovie_whenGettingMovie_shouldReturnMovie() {
        Movie stored = getMovie();
        movieRepository.save(stored);

        assertNotNull(movieService.findById(stored.getId()));
    }

    @Test
    @DisplayName("After trying to get non-stored movie, should throw exception.")
    public void storedNothing_whenGettingMovie_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> movieService.findById(1L));
    }

    @Test
    @DisplayName("After trying to save movie, should return id.")
    public void storedNothing_orMovie_whenSavingMovie_shouldReturnId() {
        Movie stored = getMovie();
        Long id = movieService.save(stored);

        assertAll(
                () -> assertNotNull(id),
                () -> assertNotNull(movieService.findById(id))
        );

        Long id2 = movieService.save(stored);

        assertAll(
                () -> assertNotNull(id2),
                () -> assertNotNull(movieService.findById(id2))
        );
    }
}
