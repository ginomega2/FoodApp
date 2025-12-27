package com.phegon.foodapp.auth_users.services;

import com.phegon.foodapp.auth_users.dtos.LoginRequest;
import com.phegon.foodapp.auth_users.dtos.LoginResponse;
import com.phegon.foodapp.auth_users.dtos.RegistrationRequest;
import com.phegon.foodapp.auth_users.entity.User;
import com.phegon.foodapp.auth_users.repository.UserRepository;
import com.phegon.foodapp.exceptions.BadRequestException;
import com.phegon.foodapp.exceptions.NotFoundException;
import com.phegon.foodapp.response.Response;
import com.phegon.foodapp.role.entity.Role;
import com.phegon.foodapp.role.repository.RoleRepository;
import com.phegon.foodapp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        log.info("Registering user: {}", registrationRequest);
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Email address already in use");

        }

        List<Role> userRoles;
        if (registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()) {
            userRoles=registrationRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(()-> new NotFoundException("Role with name " + roleName + " not found")))
                    .toList();

        }else {
            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(()-> new NotFoundException("default Customer role not found"));
            userRoles = List.of(defaultRole);
        }

        User userToSave = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .address(registrationRequest.getAddress())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(userToSave);
        log.info("User registered: {}", userToSave);


        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Usr registered")
                .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        log.info("Login request: {}", loginRequest);
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new NotFoundException("User not found"));

        if(!user.isActive()){
            throw new BadRequestException("User is not active");
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Wrong password");

        }
        String token = jwtUtils.generateToken(user.getEmail());

        List<String> roleNames = user.getRoles().stream()
                        .map(Role::getName)
                                .toList();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roleNames);

        log.info("User logged in: {}", user);

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("login succes")
                .data(loginResponse)
                .build();
    }
}
