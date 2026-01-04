package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.UserCreationRequest;
import com.cardano_lms.server.DTO.Request.UserUpdateRequest;
import com.cardano_lms.server.DTO.Request.UserUpdateRoleRequest;
import com.cardano_lms.server.DTO.Response.UserResponse;
import com.cardano_lms.server.Entity.LoginMethod;
import com.cardano_lms.server.Entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "loginMethod", source = "loginMethod")
    User toUser(UserCreationRequest request);

    default LoginMethod mapLoginMethod(String loginMethodName) {
        if (loginMethodName == null) return null;
        LoginMethod lm = new LoginMethod();
        lm.setName(loginMethodName);
        return lm;
    }

    User toUser(UserResponse userResponse);

    @Mapping(target = "bio", source = "bio")
    @Mapping(target = "hasPassword", expression = "java(user.getPassword() != null && !user.getPassword().isEmpty())")
    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
