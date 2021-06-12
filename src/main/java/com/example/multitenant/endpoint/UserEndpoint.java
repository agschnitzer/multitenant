package com.example.multitenant.endpoint;

import com.example.multitenant.endpoint.dto.UserEmailDto;
import com.example.multitenant.endpoint.dto.UserSignupDto;
import com.example.multitenant.endpoint.mapper.UserMapper;
import com.example.multitenant.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("api/v1/user")
public class UserEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserMapper userMapper;
    private final UserService userService;

    @Autowired
    public UserEndpoint(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * Signs up user.
     *
     * @param userSignupDto containing details about user.
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/signup") @ResponseStatus(HttpStatus.CREATED)
    public void save(@Valid @RequestBody UserSignupDto userSignupDto) {
        LOGGER.info("POST /api/v1/user/signup: {}", userSignupDto);
        userService.signup(userMapper.userSignupDtoToUser(userSignupDto));
    }

    /**
     * Changes email of user.
     *
     * @param userEmailDto containing new email.
     * @return changed email.
     * @throws IOException if something goes wrong during changing file names.
     */
    @PatchMapping("/email")
    public String changeEmail(@Valid @RequestBody UserEmailDto userEmailDto) throws IOException {
        LOGGER.info("PATCH /api/v1/user/email: {}", userEmailDto);
        return userService.patchEmail(userMapper.userEmailDtoToUser(userEmailDto));
    }
}
