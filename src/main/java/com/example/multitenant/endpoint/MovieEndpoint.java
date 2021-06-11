package com.example.multitenant.endpoint;

import com.example.multitenant.endpoint.dto.MovieDto;
import com.example.multitenant.endpoint.mapper.MovieMapper;
import com.example.multitenant.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("api/v1/movie")
public class MovieEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MovieMapper movieMapper;
    private final MovieService movieService;

    @Autowired
    public MovieEndpoint(MovieMapper movieMapper, MovieService movieService) {
        this.movieMapper = movieMapper;
        this.movieService = movieService;
    }

    /**
     * Finds movie with given id.
     *
     * @param id of movie.
     * @return all details of the movie.
     */
    @GetMapping("/{id}")
    public MovieDto findById(@PathVariable Long id) {
        LOGGER.info("GET /api/v1/movie/{}", id);
        return movieMapper.movieToMovieDto(movieService.findById(id));
    }

    /**
     * Saves a movie.
     *
     * @param movieDto containing all details of movie entity.
     * @return id of saved movie.
     */
    @PostMapping
    public Long save(@RequestBody MovieDto movieDto) {
        LOGGER.info("POST /api/v1/movie{}", movieDto);
        return movieService.save(movieMapper.movieDtoToMovie(movieDto));
    }
}
