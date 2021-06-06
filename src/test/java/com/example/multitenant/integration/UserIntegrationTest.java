package com.example.multitenant.integration;

import com.example.multitenant.data.UserData;
import com.example.multitenant.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class UserIntegrationTest implements UserData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("After signing up user, entity should be saved and status created.")
    public void storedNothing_whenSigningUp_thenEntityShouldBeSaved() throws Exception {
        mockMvc.perform(post("/api/v1/user/signup")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(getUserSignUpDto())))
                .andExpect(status().isCreated());

        assertTrue(userRepository.existsUserByEmailEquals(EMAIL));
    }

    @Test @Sql("classpath:user.sql")
    @DisplayName("After trying to sign up stored user, should throw exception and status bad request.")
    public void storedUser_whenSigningUp_shouldThrowException() throws Exception {
        mockMvc.perform(post("/api/v1/user/signup")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(getUserSignUpDto())))
                .andExpect(status().isBadRequest());
    }
}
