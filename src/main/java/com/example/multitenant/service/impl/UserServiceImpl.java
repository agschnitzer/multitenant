package com.example.multitenant.service.impl;

import com.example.multitenant.config.DatabaseConfig;
import com.example.multitenant.entity.User;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final DatabaseConfig databaseConfig;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, DatabaseConfig databaseConfig) {
        this.userRepository = userRepository;
        this.databaseConfig = databaseConfig;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.trace("loadUserByUsername({})", email);

        try {
            User user = findByEmail(email);
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
    public void signup(User user) throws ValidationException {
        LOGGER.trace("signup({})", user);

        if (!user.getPassword().equals(user.getConfirmation())) {
            throw new ValidationException("Password and confirmation don't match");
        } else if (existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already taken");
        }

        userRepository.save(user);
    }

    @Transactional
    @Override
    public String patchEmail(User user) throws ValidationException, IOException {
        LOGGER.trace("patchEmail({})", user);

        if (existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already taken");
        }

        String existingEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.changeUserEmail(existingEmail, user.getEmail());
        databaseConfig.renameDatasource(existingEmail, user.getEmail());

        return user.getEmail();
    }

    @Override
    public Boolean existsByEmail(String email) {
        LOGGER.trace("existsByEmail({})", email);
        return userRepository.existsUserByEmailEquals(email);
    }
}
