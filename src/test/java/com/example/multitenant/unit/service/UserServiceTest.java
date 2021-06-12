package com.example.multitenant.unit.service;

import com.example.multitenant.config.DatabaseConfig;
import com.example.multitenant.data.UserData;
import com.example.multitenant.entity.User;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;
import com.example.multitenant.exceptionhandler.exceptions.ValidationException;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;

import java.io.IOException;

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
    @DisplayName("Loading stored entity by username should return user details.")
    public void storedEntity_whenLoadingByUsername_shouldReturnUserDetails() {
        userRepository.save(getUser());
        assertNotNull(userService.loadUserByUsername(EMAIL));
    }

    @Test
    @DisplayName("Loading non-stored entity by username should throw an exception.")
    public void storedNothing_whenLoadingByUsername_shouldThrowException() {
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(EMAIL));
    }

    @Test
    @DisplayName("Getting stored entity should return the entity.")
    public void storedEntity_whenGettingEntity_shouldReturnEntity() {
        User stored = getUser();
        userRepository.save(stored);

        User entity = userService.findByEmail(stored.getEmail());

        assertAll(
                () -> assertNotNull(entity),
                () -> assertEquals(stored.getId(), entity.getId()),
                () -> assertEquals(stored.getEmail(), entity.getEmail()),
                () -> assertEquals(stored.getPassword(), entity.getPassword()),
                () -> assertEquals(stored.getConfirmation(), entity.getConfirmation())
        );
    }

    @Test
    @DisplayName("Getting non-stored entity should throw an exception.")
    public void storedNothing_whenGettingEntity_shouldThrowException() {
        assertThrows(NotFoundException.class, () -> userService.findByEmail(EMAIL));
    }

    @Test
    @DisplayName("Saving entity should persist entity.")
    public void storedNothing_whenSavingEntity_shouldPersistEntity() {
        User stored = getUser();

        userService.signup(stored);
        User entity = userService.findByEmail(EMAIL);

        assertAll(
                () -> assertEquals(stored.getId(), entity.getId()),
                () -> assertEquals(stored.getEmail(), entity.getEmail()),
                () -> assertEquals(stored.getPassword(), entity.getPassword()),
                () -> assertEquals(stored.getConfirmation(), entity.getConfirmation())
        );
    }

    @Test
    @DisplayName("Trying to save a faulty entity should throw an exception.")
    public void storedNothing_andFaultyEntity_whenSavingEntity_shouldThrowException() {
        User user = getUser();
        user.setConfirmation(WRONG_PASSWORD);
        assertThrows(ValidationException.class, () -> userService.signup(user));
    }

    @Test
    @DisplayName("Trying to save same entity twice should throw an exception.")
    public void storedEntity_whenSavingEntity_shouldThrowException() {
        User stored = getUser();
        userRepository.save(stored);

        User entity = userService.findByEmail(EMAIL);

        assertAll(
                () -> assertEquals(stored.getId(), entity.getId()),
                () -> assertEquals(stored.getEmail(), entity.getEmail()),
                () -> assertEquals(stored.getPassword(), entity.getPassword()),
                () -> assertEquals(stored.getConfirmation(), entity.getConfirmation())
        );

        assertThrows(ValidationException.class, () -> userService.signup(getUser()));
    }

    @Test
    @DisplayName("Changing email of entity should return new email.")
    public void storedEntity_whenChangingEmail_shouldReturnNewEmail() throws IOException {
        User stored = getUser();
        userRepository.save(stored);

        assertAll(
                () -> assertEquals(EMAIL, stored.getEmail()),
                () -> assertNotEquals(NEW_EMAIL, stored.getEmail()),
                () -> assertEquals(NEW_EMAIL, userService.patchEmail(getUserEmail(NEW_EMAIL)))
        );

        DatabaseConfig.renameDatasource(NEW_EMAIL, EMAIL);
    }

    @Test
    @DisplayName("Trying to change email of entity to same email should throw an exception.")
    public void storedEntity_whenChangingEmail_toSameEmail_shouldThrowException() {
        User stored = getUser();
        userRepository.save(stored);

        assertAll(
                () -> assertEquals(EMAIL, stored.getEmail()),
                () -> assertThrows(ValidationException.class, () -> userService.patchEmail(getUserEmail(stored.getEmail())))
        );
    }

    @Test
    @DisplayName("Checking if stored entity exists should return true.")
    public void storedEntity_whenCheckingEntity_shouldReturnTrue() {
        User stored = getUser();
        userRepository.save(stored);

        assertTrue(userService.existsByEmail(stored.getEmail()));
    }

    @Test
    @DisplayName("Checking if non-stored entity exists should return false.")
    public void storedNothing_whenCheckingEntity_shouldReturnTrue() {
        assertFalse(userService.existsByEmail(EMAIL));
    }
}
