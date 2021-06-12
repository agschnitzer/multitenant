package com.example.multitenant.service.impl;

import com.example.multitenant.config.DatabaseConfig;
import com.example.multitenant.entity.User;
import com.example.multitenant.exceptionhandler.exceptions.DataSourceException;
import com.example.multitenant.exceptionhandler.exceptions.NotFoundException;
import com.example.multitenant.exceptionhandler.exceptions.ValidationException;
import com.example.multitenant.repository.UserRepository;
import com.example.multitenant.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        try {
            User user = findByEmail(s);
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(), AuthorityUtils.createAuthorityList("ROLE_USER")
            );
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public User findByEmail(String email) throws NotFoundException {
        LOGGER.trace("findByEmail({})", email);

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) throw new NotFoundException(String.format("User with email: %s not found", email));
        return user.get();
    }

    @Override
    public void signUp(User user) throws ValidationException {
        LOGGER.trace("signUp({})", user);

        if (!user.getPassword().equals(user.getConfirmation())) {
            throw new ValidationException("Password and confirmation don't match");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userRepository.existsUserByEmailEquals(user.getEmail())) {
            throw new ValidationException("Email already taken");
        }
        userRepository.save(user);
    }

    @Transactional
    @Override
    public String patchEmail(User user) throws ValidationException, DataSourceException {
        LOGGER.trace("patchEmail({})", user);

        if (userRepository.existsUserByEmailEquals(user.getEmail())) {
            throw new ValidationException("Email already taken");
        }

        User existingUser = findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        try {
            DatabaseConfig.renameDatasource(existingUser.getEmail(), user.getEmail());
        } catch (IOException e) {
            throw new DataSourceException("Problem occurred during changing database identifier");
        }
        existingUser.setEmail(user.getEmail());

        return userRepository.save(existingUser).getEmail();
    }

    @Override
    public Boolean existsByEmail(String email) {
        LOGGER.trace("existsByEmail({})", email);
        return userRepository.existsUserByEmailEquals(email);
    }
}
