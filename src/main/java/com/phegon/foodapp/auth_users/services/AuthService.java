package com.phegon.foodapp.auth_users.services;

import com.phegon.foodapp.auth_users.dtos.LoginRequest;
import com.phegon.foodapp.auth_users.dtos.LoginResponse;
import com.phegon.foodapp.auth_users.dtos.RegistrationRequest;
import com.phegon.foodapp.response.Response;

public interface AuthService {

    Response<?> register(RegistrationRequest registrationRequest);
    Response<LoginResponse> login(LoginRequest loginRequest);
}
