package com.phegon.foodapp.role.services;

import com.phegon.foodapp.exceptions.BadRequestException;
import com.phegon.foodapp.exceptions.NotFoundException;
import com.phegon.foodapp.response.Response;
import com.phegon.foodapp.role.dtos.RoleDTO;
import com.phegon.foodapp.role.entity.Role;
import com.phegon.foodapp.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<RoleDTO> createRole(RoleDTO roleDTO) {
        Role role = modelMapper.map(roleDTO, Role.class);

        Role savedRole = roleRepository.save(role);


        return Response.<RoleDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role created successfully")
                .data(modelMapper.map(savedRole, RoleDTO.class))
                .build();

    }

    @Override
    public Response<RoleDTO> updateRole(RoleDTO roleDTO) {
        Role existingRole = roleRepository.findById(roleDTO.getId())
                .orElseThrow(() -> new NotFoundException("role not found"));

        if(roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new BadRequestException("role name already exists");
        }

        existingRole.setName(roleDTO.getName());
        Role updatedRole=roleRepository.save(existingRole);


        return Response.<RoleDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role updated successfully")
                .data(modelMapper.map(updatedRole, RoleDTO.class))
                .build();
    }

    @Override
    public Response<RoleDTO> getRoleById(Long id) {
        return null;
    }

    @Override
    public Response<List<RoleDTO>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO> roleDTOS = roles.stream()
                .map(role -> modelMapper.map(role,RoleDTO.class))
                .toList();

        return Response.<List<RoleDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles found")
                .data(roleDTOS)
                .build();
    }

    @Override
    public Response<?> deleteRole(Long id) {
        if(!roleRepository.existsById(id)) {
            throw new NotFoundException("role not found");
        }
        roleRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role deleted successfully")
                .build();
    }
}
