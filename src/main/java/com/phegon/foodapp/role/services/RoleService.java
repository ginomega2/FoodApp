package com.phegon.foodapp.role.services;

import com.phegon.foodapp.response.Response;
import com.phegon.foodapp.role.dtos.RoleDTO;

import java.util.List;

public interface RoleService {

    Response<RoleDTO> createRole(RoleDTO roleDTO);

    Response<RoleDTO> updateRole(RoleDTO roleDTO);

    Response<RoleDTO> getRoleById(Long id);

    Response<List<RoleDTO>> getAllRoles();

    Response<?> deleteRole(Long id);

}
