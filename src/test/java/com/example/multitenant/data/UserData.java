package com.example.multitenant.data;

import com.example.multitenant.endpoint.dto.UserAuthenticationDto;
import com.example.multitenant.endpoint.dto.UserEmailDto;
import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface UserData {

    String EMAIL = "user@example.com";
    String NEW_EMAIL = "user1@example.com";
    String PASSWORD = "SecretPassword1!";
    String WRONG_PASSWORD = "WrongPassword";

    /**
     * Builds a user entity.
     *
     * @return entity containing details about user.
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
     * @return entity containing user email details.
     */
    default User getUserEmail(String email) {
        return User.builder().email(email).build();
    }

    /**
     * Builds a user authentication dto.
     *
     * @return dto containing details about user.
     */
    default UserAuthenticationDto getUserAuthenticationDto() {
        return UserAuthenticationDto.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();
    }

    /**
     * Builds a user signup dto.
     *
     * @return dto containing details about user.
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
     * @return dto containing details about user.
     */
    default UserEmailDto getUserEmailDto() {
        return UserEmailDto.builder()
                .email(NEW_EMAIL)
                .build();
    }

    /**
     * Builds a user authentication dto.
     *
     * @return dto containing details about user.
     * @throws JsonProcessingException if something goes wrong during parsing json.
     */
    default String getUserAuthenticationDtoJson() throws JsonProcessingException {
        return getUserMapper().writeValueAsString(getUserAuthenticationDto());
    }

    /**
     * Builds a user signup dto.
     *
     * @return json object containing details about user.
     * @throws JsonProcessingException if something goes wrong during parsing json.
     */
    default String getUserSignUpDtoJson() throws JsonProcessingException {
        return getUserMapper().writeValueAsString(getUserSignUpDto());
    }

    /**
     * Builds a user email dto.
     *
     * @return json object containing user email details.
     * @throws JsonProcessingException if something goes wrong during parsing json.
     */
    default String getUserEmailDtoJson() throws JsonProcessingException {
        return getUserMapper().writeValueAsString(getUserEmailDto());
    }

    /**
     * Builds an object mapper.
     *
     * @return object mapper.
     */
    default ObjectMapper getUserMapper() {
        return new ObjectMapper();
    }
}
