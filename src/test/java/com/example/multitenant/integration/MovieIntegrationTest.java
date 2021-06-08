package com.example.multitenant.integration;

import com.example.multitenant.data.MovieData;
import com.example.multitenant.data.UserData;
import com.example.multitenant.repository.MovieRepository;
import com.example.multitenant.security.JwtTokenizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "user@example.com", password = "SecretPassword1!")
public class MovieIntegrationTest implements MovieData, UserData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    // @Sql("classpath:movie.sql")
    @DisplayName("After getting stored movie, dto should be returned.")
    public void storedMovie_whenGettingMovie_shouldReturnDto() throws Exception {
        movieRepository.save(getMovie());

        mockMvc.perform(get("/api/v1/movie/{id}", 1L)
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("After getting not stored movie, should throw exception.")
    public void storedNothing_whenGettingMovie_shouldThrowException() throws Exception {
        mockMvc.perform(get("/api/v1/movie/{id}", 1L)
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }
}
