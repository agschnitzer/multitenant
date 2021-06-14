package com.example.multitenant.repository;

import com.example.multitenant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Changes email of user.
     *
     * @param oldEmail of user which is going to change.
     * @param newEmail of user that overrides old one.
     */
    @Modifying
    @Query("UPDATE User u SET u.email = :newEmail WHERE u.email = :oldEmail")
    void changeUserEmail(@Param("oldEmail") String oldEmail, @Param("newEmail") String newEmail);

    /**
     * Checks if user exists.
     *
     * @param email of user.
     * @return true if user with given email exists.
     */
    Boolean existsUserByEmailEquals(String email);
}
