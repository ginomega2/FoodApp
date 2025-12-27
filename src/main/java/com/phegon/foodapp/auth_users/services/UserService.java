package com.phegon.foodapp.auth_users.services;

import com.phegon.foodapp.auth_users.dtos.UserDTO;
import com.phegon.foodapp.auth_users.entity.User;
import com.phegon.foodapp.response.Response;

import java.util.List;

public interface UserService {

    User getCurrentLoggedInUser();
    Response<List<UserDTO>> getAllUsers();
    Response<UserDTO> getOwnAccountDetails();
    Response<?> updateOwnAccount(UserDTO userDTO);
    Response<?> deactivateOwnAccount();

}
