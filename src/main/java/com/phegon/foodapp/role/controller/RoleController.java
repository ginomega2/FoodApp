package com.phegon.foodapp.role.controller;

import com.phegon.foodapp.response.Response;
import com.phegon.foodapp.role.dtos.RoleDTO;
import com.phegon.foodapp.role.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
//@PreAuthorize("hasAnyAuthority('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Response<RoleDTO>> createRole
            (@RequestBody @Valid RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.createRole(roleDTO));

    }

    @PutMapping
    public ResponseEntity<Response<RoleDTO>> updateRole
            (@RequestBody @Valid RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.updateRole(roleDTO));

    }

    @GetMapping
    public ResponseEntity<Response<List<RoleDTO>>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<?>> deleteRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.deleteRole(id));


    }



}
