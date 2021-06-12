package com.example.multitenant.service;

import com.example.multitenant.entity.Movie;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;

public interface MovieService {

    /**
     * Finds movie with given id.
     *
     * @param id of movie.
     * @return all details about the movie.
     * @throws NotFoundException if movie with given id doesn't exist.
     */
    Movie findById(Long id) throws NotFoundException;

    /**
     * Saves movie.
     *
     * @param movie containing details about movie.
     * @return id of saved movie.
     */
    Long save(Movie movie);
}
