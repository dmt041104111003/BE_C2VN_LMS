package com.cardano_lms.server.Mapper;

import com.cardano_lms.server.DTO.Request.LoginMethodRequest;
import com.cardano_lms.server.DTO.Response.LoginMethodResponse;
import com.cardano_lms.server.Entity.LoginMethod;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginMethodMapper {
    LoginMethod toLoginMethod(LoginMethodRequest loginMethodRequest);
    LoginMethodResponse toLoginMethodResponse(LoginMethod loginMethod);
}
