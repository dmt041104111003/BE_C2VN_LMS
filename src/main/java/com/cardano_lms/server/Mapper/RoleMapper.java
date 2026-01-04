package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.RoleRequest;
import com.cardano_lms.server.DTO.Response.RoleResponse;
import com.cardano_lms.server.Entity.Role;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
