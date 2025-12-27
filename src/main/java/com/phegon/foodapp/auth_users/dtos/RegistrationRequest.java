package com.phegon.foodapp.auth_users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegistrationRequest {

    @NotBlank(message = " requerido")
    private String name;

    @NotBlank(message = " requerido")
    @Email(message = "email invalido")
    private String email;

    @NotBlank(message = "password  requerido")
    @Size(min = 3, message = "minimo 3 caracteres")
    private String password;

    @NotBlank(message = " requerido")
    private String address;

    @NotBlank(message = " requerido")
    private String phoneNumber;

    private List<String> roles;
}
