package com.example.multitenant.data;

import com.example.multitenant.endpoint.dto.UserEmailDto;
import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface UserData {

    String EMAIL = "user@example.com";
    String NEW_EMAIL = "user1@example.com";
    String PASSWORD = "SecretPassword1!";

    /**
     * Builds a user entity.
     *
     * @return user entity containing valid email, password and confirmation.
     */
    default User getUser() {
        return User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .confirmation(PASSWORD)
                .build();
    }

    /**
     * Builds a user entity.
     *
     * @param email of user.
     * @return user entity containing only new email.
     */
    default User getUserEmail(String email) {
        return User.builder().email(email).build();
    }

    /**
     * Builds a user signup dto.
     *
     * @return user dto containing valid email, password and confirmation.
     */
    default UserSignupDto getUserSignUpDto() {
        return UserSignupDto.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .confirmation(PASSWORD)
                .build();
    }

    /**
     * Builds a user email dto.
     *
     * @return user dto containing valid email.
     */
    default UserEmailDto getUserEmailDto() {
        return UserEmailDto.builder()
                .email(NEW_EMAIL)
                .build();
    }

    /**
     * Builds a user signup dto.
     *
     * @return user dto containing valid email, password and confirmation in json format.
     * @throws JsonProcessingException if something goes wrong during parsing json.
     */
    default String getUserDtoJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(getUserSignUpDto());
    }
}
