package com.example.multitenant.unit.service;

import com.example.multitenant.data.UserData;
import com.example.multitenant.entity.User;
import com.example.multitenant.exceptionhandler.exceptions.DataSourceException;
import com.example.multitenant.exceptionhandler.exceptions.ValidationException;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "user@example.com", password = "SecretPassword1!")
public class UserServiceTest implements UserData {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("After signing up user, entity should be saved.")
    public void storedNothing_whenSigningUp_thenEntityShouldBeSaved() {
        User user = getUser();

        userService.signUp(user);
        User entity = userService.findByEmail(EMAIL);

        assertAll(
                () -> assertEquals(user.getEmail(), entity.getEmail()),
                () -> assertEquals(user.getPassword(), entity.getPassword()),
                () -> assertEquals(user.getConfirmation(), entity.getConfirmation())
        );

        UserDetails userDetails = userService.loadUserByUsername(entity.getEmail());

        assertAll(
                () -> assertEquals(entity.getEmail(), userDetails.getUsername()),
                () -> assertEquals(entity.getPassword(), userDetails.getPassword())
        );
    }

    @Test
    @DisplayName("After trying to sign up faulty user, an exception should be thrown.")
    public void storedNothing_andFaultyUser_whenSigningUp_shouldThrowException() {
        User user = getUser();
        user.setConfirmation(PASSWORD + "wrong");
        assertThrows(ValidationException.class, () -> userService.signUp(user));
    }

    @Test
    // @Sql("classpath:user.sql")
    @DisplayName("After trying to sign up user with same email address as existing one, an exception should be thrown.")
    public void storedUser_whenSigningUp_shouldThrowException() {
        userRepository.save(getUser());

        assertNotNull(userService.findByEmail(EMAIL));
        assertThrows(ValidationException.class, () -> userService.signUp(getUser()));
    }
}
