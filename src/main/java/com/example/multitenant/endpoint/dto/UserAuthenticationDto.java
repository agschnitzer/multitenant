package com.example.multitenant.endpoint.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter @Setter @ToString
public class UserAuthenticationDto {

    @NotNull(message = "Email must not be null")
    @Size(min = 6, max = 60, message = "Email must be between 6 and 60 characters")
    @Pattern(regexp = "^([\\wäöüçÄÖÜß]+)@([\\wäöüçÄÖÜß]+)\\.[a-zA-Z]{2,3}$", message = "Email is invalid")
    private String email;

    @NotNull(message = "Password must not be null")
    @Size(min = 10, max = 60, message = "Password must be between 10 and 60 characters")
    @Pattern(regexp = "^[^;:<>'`\"]*$", message = "Password must not contain any of these characters: ;:<>'`\"")
    private String password;
}
