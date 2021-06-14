package com.example.multitenant.integration;

import com.example.multitenant.config.DatabaseConfig;
import com.example.multitenant.data.UserData;
import com.example.multitenant.endpoint.dto.UserAuthenticationDto;
import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.security.JwtTokenizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserIntegrationTest implements UserData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseConfig databaseConfig;

    @BeforeEach
    public void beforeEach() {
        DatabaseConfig.DBContextHolder.setDefault();
        userRepository.deleteAll();
    }

    @AfterEach
    public void afterEach() {
        DatabaseConfig.DBContextHolder.setDefault();
        userRepository.deleteAll();
    }

    private void saveEntity() throws Exception {
        mockMvc.perform(post("/api/v1/user/signup")
                .contentType("application/json")
                .content(getUserSignUpDtoJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Saving entity should return status is created.")
    public void storedNothing_whenSavingEntity_shouldReturnStatusIsCreated() throws Exception {
        saveEntity();
    }

    @Test
    @DisplayName("Saving entity with faulty data should return status bad request.")
    public void storedNothing_withFaultyData_whenSavingEntity_shouldReturnStatusBadRequest() throws Exception {
        String[] faultyEmails = new String[]{"faulty.email", "", "user@example", "123", "...", ",,dd"};

        for (String email : faultyEmails) {
            UserSignupDto dto = getUserSignUpDto();
            dto.setEmail(email);

            mockMvc.perform(post("/api/v1/user/signup")
                    .contentType("application/json")
                    .content(getUserMapper().writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        String[] faultyPasswords = new String[]{"12345", "password;", "SELECT *\"", "user,password"};

        for (String password : faultyPasswords) {
            UserSignupDto dto = getUserSignUpDto();
            dto.setPassword(password);

            mockMvc.perform(post("/api/v1/user/signup")
                    .contentType("application/json")
                    .content(getUserMapper().writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("Trying to save stored entity twice should return status bad request.")
    public void storedEntity_whenSavingEntity_shouldReturnStatusBadRequest() throws Exception {
        saveEntity();

        mockMvc.perform(post("/api/v1/user/signup")
                .contentType("application/json")
                .content(getUserSignUpDtoJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Changing email of stored entity should return changed email and status ok.")
    public void storedEntity_whenChangingEmail_shouldReturnChangedEmail_andStatusOk() throws Exception {
        saveEntity();

        MvcResult result = mockMvc.perform(patch("/api/v1/user/email")
                .header("Authorization", jwtTokenizer.createToken(EMAIL, Collections.singletonList("ROLE_USER")))
                .contentType("application/json")
                .content(getUserEmailDtoJson()))
                .andExpect(status().isOk()).andReturn();

        assertEquals(NEW_EMAIL, result.getResponse().getContentAsString());

        databaseConfig.renameDatasource(NEW_EMAIL, EMAIL);
    }

    @Test
    @DisplayName("Trying to change email of stored entity with invalid token should return status unauthorized.")
    public void storedEntity_whenChangingEmail_withInvalidToken_shouldReturnStatusUnauthorized() throws Exception {
        saveEntity();

        mockMvc.perform(patch("/api/v1/user/email")
                .contentType("application/json")
                .content(getUserEmailDtoJson()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/user/email")
                .header("Authorization", jwtTokenizer.createToken(null, Collections.singletonList("ROLE_USER")))
                .contentType("application/json")
                .content(getUserEmailDtoJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticating stored entity should return token and status ok.")
    public void storedEntity_whenAuthenticatingEntity_shouldReturnToken_andStatusOk() throws Exception {
        saveEntity();

        MvcResult result = mockMvc.perform(post("/api/v1/authentication")
                .contentType("application/json")
                .content(getUserAuthenticationDtoJson()))
                .andExpect(status().isOk()).andReturn();

        assertTrue(result.getResponse().getContentAsString().startsWith("Bearer "));
    }

    @Test
    @DisplayName("Trying to authenticate non-stored entity should return status unauthorized.")
    public void storedNothing_whenAuthenticatingEntity_shouldReturnStatusUnauthorized() throws Exception {
        saveEntity();

        UserAuthenticationDto dto = getUserAuthenticationDto();
        dto.setEmail(NEW_EMAIL);

        mockMvc.perform(post("/api/v1/authentication")
                .contentType("application/json")
                .content(getUserMapper().writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Trying to authenticate entity with wrong format should return status unauthorized.")
    public void storedNothing_whenAuthenticatingEntity_withWrongFormat_shouldReturnStatusUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/authentication")
                .content(getUserSignUpDtoJson()))
                .andExpect(status().isUnauthorized());
    }
}
