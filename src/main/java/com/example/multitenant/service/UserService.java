package com.example.multitenant.service;

import com.example.multitenant.entity.User;
import com.example.multitenant.exceptionhandler.exceptions.DataSourceException;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;
import com.example.multitenant.exceptionhandler.exceptions.ValidationException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    /**
     * Finds user with given email address.
     *
     * @param email address of user.
     * @return user containing email and password.
     * @throws NotFoundException if user couldn't be found.
     */
    User findByEmail(String email) throws NotFoundException;

    /**
     * Registers new user.
     *
     * @param user containing email, password and confirmation.
     * @throws ValidationException if password and confirmation don't match.
     */
    void signUp(User user) throws ValidationException;

    /**
     * Patches email of user.
     *
     * @param user containing new email address.
     * @return new email address.
     * @throws ValidationException if user with given email already exists.
     * @throws DataSourceException if something goes wrong during changing file names.
     */
    String patchEmail(User user) throws ValidationException, DataSourceException;
}
