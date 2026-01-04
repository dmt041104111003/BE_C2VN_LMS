package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Request.LoginMethodRequest;
import com.cardano_lms.server.DTO.Response.LoginMethodResponse;
import com.cardano_lms.server.Entity.LoginMethod;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.LoginMethodMapper;
import com.cardano_lms.server.Repository.LoginMethodRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginMethodService {
    LoginMethodRepository loginMethodRepository;
    LoginMethodMapper loginMethodMapper;

    public LoginMethodResponse createNewLoginMethod(LoginMethodRequest loginMethodRequest) {
        if(loginMethodRepository.existsByName(loginMethodRequest.getName())) {
            throw new AppException(ErrorCode.METHOD_HAS_BEEN_EXISTED);
        }
        LoginMethod newLoginMethod = loginMethodRepository
                .save(loginMethodMapper.toLoginMethod(loginMethodRequest));
        return loginMethodMapper.toLoginMethodResponse(newLoginMethod);
    }

    public List<LoginMethodResponse> getAllLoginMethods() {
        return loginMethodRepository.findAll().stream().map(loginMethodMapper::toLoginMethodResponse).toList();
    }

    public void deleteLoginMethod(String loginMethodName) {
        loginMethodRepository.deleteById(loginMethodName);
    }
}
