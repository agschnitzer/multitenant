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

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserMapperTest implements UserData {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("After mapping UserSignupDto to User, entity should contain same properties as dto.")
    public void givenUserSignupDto_whenMappingToUser_entityHasAllProperties() {
        UserSignupDto dto = getUserSignUpDto();
        User entity = userMapper.userSignupDtoToUser(dto);

        assertAll(
                () -> assertEquals(dto.getEmail(), entity.getEmail()),
                () -> assertEquals(dto.getPassword(), entity.getPassword()),
                () -> assertEquals(dto.getConfirmation(), entity.getConfirmation())
        );
    }

    @Test
    @DisplayName("After mapping UserEmailDto to User, entity should contain same properties as dto.")
    public void givenUserEmailDto_whenMappingToUser_entityHasAllProperties() {
        UserEmailDto dto = getUserEmailDto();
        User entity = userMapper.userEmailDtoToUser(dto);

        assertAll(
                () -> assertEquals(dto.getEmail(), entity.getEmail()),
                () -> assertNull(entity.getPassword()),
                () -> assertNull(entity.getConfirmation())
        );
    }

    @Test
    @DisplayName("After mapping empty object, result should also be empty.")
    public void givenNothing_whenMappingToUser_entityShouldBeNull() {
        assertNull(userMapper.userSignupDtoToUser(null));
        assertNull(userMapper.userEmailDtoToUser(null));
    }
}
