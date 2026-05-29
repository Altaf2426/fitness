package com.fitness.userservice.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "email must required")
    @Email(message = "email required")
    private String email;

    private String password;
    private String firstName;
    private String lastName;
}
