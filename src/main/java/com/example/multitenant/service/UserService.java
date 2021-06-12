package com.example.multitenant.service;

import com.example.multitenant.entity.User;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;
import com.example.multitenant.exceptionhandler.exceptions.ValidationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

public interface UserService extends UserDetailsService {

    /**
     * Finds user with given email.
     *
     * @param email of user.
     * @return all details about the user.
     * @throws NotFoundException if user with given email doesn't exist.
     */
    User findByEmail(String email) throws NotFoundException;

    /**
     * Signs up user.
     *
     * @param user containing details about user.
     * @throws ValidationException if password and confirmation don't match.
     */
    void signup(User user) throws ValidationException;

    /**
     * Changes email of user.
     *
     * @param user containing new email.
     * @return changed email.
     * @throws ValidationException if user with given email already exists.
     * @throws IOException if something goes wrong during changing file names.
     */
    String patchEmail(User user) throws ValidationException, IOException;

    /**
     * Checks if user exists.
     *
     * @param email of user.
     * @return true if the user with the given email exists.
     */
    Boolean existsByEmail(String email);
}
