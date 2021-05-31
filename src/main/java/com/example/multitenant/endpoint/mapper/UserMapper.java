package com.example.multitenant.endpoint.mapper;

import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    /**
     * Maps incoming user register dto to user entity.
     *
     * @param userSignupDto contains email, password and confirmation.
     * @return mapped user entity containing email and password only once.
     */
    User userSignupDtoToUser(UserSignupDto userSignupDto);
}