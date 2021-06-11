package com.example.multitenant.service.impl;

import com.example.multitenant.entity.Movie;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;
import com.example.multitenant.repository.MovieRepository;
import com.example.multitenant.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
public class MovieServiceImpl implements MovieService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MovieRepository movieRepository;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public Movie findById(Long id) throws NotFoundException {
        LOGGER.trace("findById({})", id);

        Optional<Movie> movie = movieRepository.findById(id);
        if (movie.isEmpty()) throw new NotFoundException(String.format("Movie with id: %d not found", id));
        return movie.get();
    }

    @Override
    public Long save(Movie movie) {
        LOGGER.trace("save({})", movie);

        return movieRepository.save(movie).getId();
    }
}
