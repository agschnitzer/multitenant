package com.example.multitenant.data;

import com.example.multitenant.endpoint.dto.UserEmailDto;
import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.entity.User;

public interface UserData {

    String EMAIL = "user@example.com";
    String NEW_EMAIL = "user1@example.com";
    String PASSWORD = "SecretPassword1!";

    /**
     * Builds an user entity.
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
     * Builds an user signup dto.
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
     * Builds an user email dto.
     *
     * @return user dto containing valid email.
     */
    default UserEmailDto getUserEmailDto() {
        return UserEmailDto.builder()
                .email(NEW_EMAIL)
                .build();
    }
}
