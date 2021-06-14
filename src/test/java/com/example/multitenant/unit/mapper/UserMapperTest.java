package com.example.multitenant.unit.mapper;

import com.example.multitenant.data.UserData;
import com.example.multitenant.endpoint.dto.UserEmailDto;
import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.endpoint.mapper.UserMapper;
import com.example.multitenant.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserMapperTest implements UserData {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("After mapping dto to entity, entity should contain all properties of dto.")
    public void givenDto_whenMappingToEntity_entityHasAllProperties() {
        UserSignupDto signUpDto = getUserSignUpDto();
        User signUpEntity = userMapper.userSignupDtoToUser(signUpDto);

        assertAll(
                () -> assertEquals(signUpDto.getEmail(), signUpEntity.getEmail()),
                () -> assertEquals(signUpDto.getPassword(), signUpEntity.getPassword()),
                () -> assertEquals(signUpDto.getConfirmation(), signUpEntity.getConfirmation())
        );

        UserEmailDto emailDto = getUserEmailDto();
        User emailEntity = userMapper.userEmailDtoToUser(emailDto);

        assertAll(
                () -> assertEquals(emailDto.getEmail(), emailEntity.getEmail()),
                () -> assertNull(emailEntity.getPassword()),
                () -> assertNull(emailEntity.getConfirmation())
        );
    }

    @Test
    @DisplayName("After mapping null, result should also be null.")
    public void givenNothing_whenMappingToEntity_resultShouldBeNull() {
        assertNull(userMapper.userSignupDtoToUser(null));
        assertNull(userMapper.userEmailDtoToUser(null));
    }
}
