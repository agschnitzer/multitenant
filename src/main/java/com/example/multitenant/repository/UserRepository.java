package com.example.multitenant.repository;

import com.example.multitenant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds user with given email address.
     *
     * @param email address of user.
     * @return object which might contain the requested user.
     */
    Optional<User> findByEmail(String email);
}
