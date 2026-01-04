package com.cardano_lms.server.Service;

import com.cardano_lms.server.Entity.LoginMethod;
import com.cardano_lms.server.Repository.*;
import com.cardano_lms.server.constant.PredefineLoginMethod;
import com.cardano_lms.server.constant.PredefinedRole;
import com.cardano_lms.server.constant.UserStatus;
import com.cardano_lms.server.DTO.Request.UserCreationRequest;
import com.cardano_lms.server.DTO.Request.UpdateNameRequest;
import com.cardano_lms.server.DTO.Request.UpdateProfileRequest;
import com.cardano_lms.server.DTO.Request.ChangePasswordRequest;
import com.cardano_lms.server.DTO.Request.UserUpdateRoleRequest;
import com.cardano_lms.server.DTO.Response.UserResponse;
import com.cardano_lms.server.Entity.Role;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.UserMapper;
import com.cardano_lms.server.Specification.UserSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    LoginMethodRepository loginMethodRepository;

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        String loginMethod = getString(request);

        User user = new User();
        user.setRole(roleRepository.findById(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)));
        
        if (PredefineLoginMethod.EMAIL_PASSWORD_METHOD.equals(loginMethod)) {
            user.setStatus(UserStatus.INACTIVE);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }

        
        LoginMethod loginMethodEntity = loginMethodRepository.findById(Objects.requireNonNull(loginMethod))
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_METHOD_IS_REQUIRED));
        user.setLoginMethod(loginMethodEntity);

        switch (loginMethod) {
            case PredefineLoginMethod.EMAIL_PASSWORD_METHOD:
                if (request.getPassword() == null || request.getPassword().isBlank()) {
                    throw new AppException(ErrorCode.PASSWORD_REQUIRED);
                }
                if (request.getEmail() == null || request.getEmail().isBlank()) {
                    throw new AppException(ErrorCode.EMAIL_REQUIRED);
                }
                if (userRepository.existsByEmail(request.getEmail()))
                    throw new AppException(ErrorCode.USER_EXISTED);

                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                if (request.getFullName() == null || request.getFullName().isBlank()) {
                    String reqEmail = request.getEmail();
                    String local = (reqEmail != null && reqEmail.contains("@"))
                            ? reqEmail.substring(0, reqEmail.indexOf('@'))
                            : "User";
                    user.setFullName(local);
                }
                break;

            case PredefineLoginMethod.WALLET_METHOD:
                if (request.getWalletAddress() == null || request.getWalletAddress().isBlank()) {
                    throw new AppException(ErrorCode.WALLET_ADDRESS_REQUIRED);
                }
                if (userRepository.existsByWalletAddress(request.getWalletAddress()))
                    throw new AppException(ErrorCode.USER_EXISTED);

                String wa = request.getWalletAddress();
                if (request.getFullName() == null || request.getFullName().isBlank()) {
                    String shortWa = wa.length() > 10 ? wa.substring(0, 6) + "..." + wa.substring(wa.length() - 4) : wa;
                    user.setFullName("Wallet " + shortWa);
                }
                user.setWalletAddress(wa);
                break;

            case PredefineLoginMethod.GOOGLE_METHOD:
                if (request.getGoogle() == null || request.getGoogle().isBlank()) {
                    throw new AppException(ErrorCode.MISSING_CREDENTIALS);
                }
                if (userRepository.findByGoogle(request.getGoogle()).isPresent())
                    throw new AppException(ErrorCode.USER_EXISTED);

                user.setGoogle(request.getGoogle());
                break;

            case PredefineLoginMethod.GITHUB_METHOD:
                if (request.getGithub() == null || request.getGithub().isBlank()) {
                    throw new AppException(ErrorCode.MISSING_CREDENTIALS);
                }
                if (userRepository.findByGithub(request.getGithub()).isPresent())
                    throw new AppException(ErrorCode.USER_EXISTED);

                user.setGithub(request.getGithub());
                break;

            default:
                throw new AppException(ErrorCode.LOGIN_METHOD_IS_REQUIRED);
        }
        if (request.getFullName() != null && !request.getFullName().isBlank())
            user.setFullName(request.getFullName());

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    private static String getString(UserCreationRequest request) {
        String loginMethod = request.getLoginMethod();
        if (loginMethod == null || loginMethod.isBlank()) {
            throw new AppException(ErrorCode.LOGIN_METHOD_IS_REQUIRED);
        }

        if (!loginMethod.equals(PredefineLoginMethod.EMAIL_PASSWORD_METHOD) &&
                !loginMethod.equals(PredefineLoginMethod.WALLET_METHOD) &&
                !loginMethod.equals(PredefineLoginMethod.GOOGLE_METHOD) &&
                !loginMethod.equals(PredefineLoginMethod.GITHUB_METHOD)) {
            throw new AppException(ErrorCode.LOGIN_METHOD_IS_REQUIRED);
        }
        return loginMethod;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (PredefinedRole.ADMIN_ROLE.equalsIgnoreCase(user.getRole().getName())) {
            throw new AppException(ErrorCode.CANNOT_BAN_ADMIN);
        }
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findById(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserResponse response = userMapper.toUserResponse(user);
        response.setBio(user.getBio());
        return response;
    }

    @Transactional
    public UserResponse updateMyName(UpdateNameRequest request) {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        user.setFullName(request.getFullName().trim());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }

        User saved = userRepository.save(user);
        UserResponse response = userMapper.toUserResponse(saved);
        response.setBio(saved.getBio());
        return response;
    }

    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getLoginMethod() == null
                || !PredefineLoginMethod.EMAIL_PASSWORD_METHOD.equals(user.getLoginMethod().getName())) {
            throw new AppException(ErrorCode.LOGIN_METHOD_NOT_SUPPORTED);
        }

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()
                || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new AppException(ErrorCode.MISSING_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') and #userId != authentication.name")
    public UserResponse updateUserRole(String userId, UserUpdateRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getRole() != null) {
            Role role = roleRepository.findById(request.getRole())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
            user.setRole(role);
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN') and #userId != authentication.name")
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (PredefinedRole.ADMIN_ROLE.equalsIgnoreCase(user.getRole().getName())) {
            throw new AppException(ErrorCode.CANNOT_BAN_ADMIN);
        }

        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public Page<UserResponse> searchUsers(String keyword, String role, String status, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(UserSpecification.searchKeyword(keyword));
        }
        if (role != null) {
            spec = spec.and(UserSpecification.hasRole(role));
        }
        if (status != null) {
            spec = spec.and(UserSpecification.hasStatus(status));
        }

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toUserResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        UserResponse response = userMapper.toUserResponse(user);
        response.setBio(user.getBio());
        return response;
    }

    
    public UserResponse getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        UserResponse response = userMapper.toUserResponse(user);
        response.setBio(user.getBio());
        return response;
    }
}
