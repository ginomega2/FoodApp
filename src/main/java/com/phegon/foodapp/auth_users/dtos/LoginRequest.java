package com.phegon.foodapp.auth_users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email es requerido")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "passwrod requerido")
    private String password;
}
