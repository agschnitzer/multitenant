package com.example.multitenant.integration;

import com.example.multitenant.config.DatabaseConfig;
import com.example.multitenant.data.MovieData;
import com.example.multitenant.data.UserData;
import com.example.multitenant.endpoint.dto.MovieDto;
import com.example.multitenant.repository.MovieRepository;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.security.JwtTokenizer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MovieIntegrationTest implements MovieData, UserData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @BeforeEach
    public void beforeEach() {
        DatabaseConfig.DBContextHolder.setDefault();
        userRepository.deleteAll();
        userRepository.save(getUser());

        DatabaseConfig.DBContextHolder.setContext(EMAIL);
    }

    @AfterEach
    public void afterEach() {
        DatabaseConfig.DBContextHolder.setDefault();
        userRepository.deleteAll();

        DatabaseConfig.DBContextHolder.setContext(EMAIL);
        movieRepository.deleteAll();
    }

    @Test
    @DisplayName("Getting stored entity should return a dto and status ok.")
    public void storedEntity_whenGettingEntity_shouldReturnDto_andStatusIsOk() throws Exception {
        MvcResult stored = mockMvc.perform(post("/api/v1/movie")
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER")))
                .contentType("application/json")
                .content(getMovieDtoJson()))
                .andExpect(status().isOk()).andReturn();

        Long id = Long.parseLong(stored.getResponse().getContentAsString());

        MvcResult result = mockMvc.perform(get("/api/v1/movie/{id}", id)
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();

        MovieDto dto = getMovieDto(result.getResponse().getContentAsString());
        assertEquals(id, dto.getId());
    }

    @Test
    @DisplayName("Trying to get non-stored entity should throw an exception and status not found.")
    public void storedNothing_whenGettingEntity_shouldThrowException_andStatusNotFound() throws Exception {
        movieRepository.deleteAll();

        mockMvc.perform(get("/api/v1/movie/{id}", ID)
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Saving entity should return status ok.")
    public void storedNothing_whenSavingMovie_shouldReturnStatusOk() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER")))
                .contentType("application/json")
                .content(getMovieDtoJson()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Trying to reach endpoint with invalid token should return status unauthorized.")
    public void storedNothing_orEntity_whenReachingEndpoint_withInvalidToken_shouldThrowException() throws Exception {
        mockMvc.perform(post("/api/v1/movie")
                .contentType("application/json")
                .content(getMovieDtoJson())).andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/movie")
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER")))
                .contentType("application/json")
                .content(getMovieDtoJson()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/movie")
                .header("Authorization", jwtTokenizer.createToken(null, Collections.singletonList("ROLE_USER")))
                .contentType("application/json")
                .content(getMovieDtoJson()))
                .andExpect(status().isUnauthorized());
    }
}
