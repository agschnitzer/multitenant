package com.example.multitenant.endpoint.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter @Setter @ToString
@Builder @AllArgsConstructor @NoArgsConstructor
public class UserEmailDto {

    @NotNull(message = "Email must not be null")
    @Size(min = 6, max = 60, message = "Email must be between 6 and 60 characters")
    @Pattern(regexp = "^([\\wäöüçÄÖÜß]+)@([\\wäöüçÄÖÜß]+)\\.[a-zA-Z]{2,3}$", message = "Email is invalid")
    private String email;
}
