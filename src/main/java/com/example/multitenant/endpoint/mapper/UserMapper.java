package com.example.multitenant.endpoint.mapper;

import com.example.multitenant.endpoint.dto.UserEmailDto;
import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    /**
     * Maps a user signup dto to a user entity.
     *
     * @param userSignupDto dto with valid email and password.
     * @return an entity containing all dto details.
     */
    User userSignupDtoToUser(UserSignupDto userSignupDto);

    /**
     * Maps a user email dto to a user entity.
     *
     * @param userEmailDto dto with valid email.
     * @return an entity containing all dto details.
     */
    User userEmailDtoToUser(UserEmailDto userEmailDto);
}
