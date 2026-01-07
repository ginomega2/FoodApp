package com.phegon.foodapp.auth_users.controller;

import com.phegon.foodapp.auth_users.dtos.UserDTO;
import com.phegon.foodapp.auth_users.services.UserService;
import com.phegon.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers() {
        /**
         * Retrieves a list of all users.
         * This endpoint is accessible only to users with the 'ADMIN' authority.
         * @return A ResponseEntity containing a Response object with a list of UserDTOs.
         */
    return ResponseEntity.ok(userService.getAllUsers());


    }


    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<?>> updateOwnAccount(
            @ModelAttribute UserDTO userDTO,
            @RequestPart(value = "imageFile", required = false ) MultipartFile imageFile
    ){
        userDTO.setImageFile(imageFile);
        return ResponseEntity.ok(userService.updateOwnAccount(userDTO));

    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<Response<?>> deactivateOwnAccountDetails(){
        return ResponseEntity.ok(userService.deactivateOwnAccount());

    }

    @GetMapping("/account")
    public ResponseEntity<Response<UserDTO>> getOwnAccountDetails(){
        return ResponseEntity.ok(userService.getOwnAccountDetails());


    }

}
