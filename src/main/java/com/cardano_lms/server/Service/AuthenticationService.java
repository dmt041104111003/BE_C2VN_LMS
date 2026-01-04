package com.cardano_lms.server.Service;

import com.cardano_lms.server.constant.PredefineLoginMethod;
import com.cardano_lms.server.constant.UserStatus;
import com.cardano_lms.server.DTO.Request.*;
import com.cardano_lms.server.DTO.Response.AuthenticationResponse;
import com.cardano_lms.server.DTO.Response.IntrospectResponse;
import com.cardano_lms.server.DTO.Response.LogoutResponse;
import com.cardano_lms.server.Entity.InvalidatedToken;
import com.cardano_lms.server.Entity.Role;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.UserMapper;
import com.cardano_lms.server.Repository.InvalidatedTokenRepository;
import com.cardano_lms.server.Repository.LoginMethodRepository;
import com.cardano_lms.server.Repository.UserRepository;
import com.cardano_lms.server.Entity.LoginMethod;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    UserService userService;
    UserMapper  userMapper;
    NonceService nonceService;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerkey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.validduration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshableduration}")
    protected long REFRESHABLE_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        if (request.getLoginMethod() == null || request.getLoginMethod().isEmpty()) {
            throw new AppException(ErrorCode.LOGIN_METHOD_IS_REQUIRED);
        }

        if (request.getLoginMethod().equals(PredefineLoginMethod.EMAIL_PASSWORD_METHOD)) {
            String principal = request.getEmail();
            if (principal == null || principal.isBlank() || request.getPassword() == null) {
                throw new AppException(ErrorCode.MISSING_CREDENTIALS);
            }

            var user = userRepository.findByEmailIgnoreCase(principal)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw new AppException(ErrorCode.LOGIN_METHOD_NOT_SUPPORTED);
            }
            if(user.getStatus().equals(UserStatus.BANNED))
                throw new AppException(ErrorCode.BANED);
            if(user.getStatus().equals(UserStatus.INACTIVE))
                throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

            if (!authenticated) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            var token = generateToken(user);
            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();
        }

        if (request.getLoginMethod().equals(PredefineLoginMethod.WALLET_METHOD)) {

            if (request.getAddress() == null
                    || request.getSignature() == null
                    || request.getKey() == null
                    || request.getNonce() == null) {
                throw new AppException(ErrorCode.MISSING_CREDENTIALS);
            }

            Boolean isValid = nonceService.validateNonce(
                    request.getNonce(),
                    request.getSignature(),
                    request.getKey()
            );

            if (!isValid) {
                throw new AppException(ErrorCode.DONT_HAVE_PERMISSION_WITH_WALLET);
            }

            var user = userRepository.findByWalletAddress(request.getAddress())
                    .orElseGet(() -> {

                        UserCreationRequest createReq = UserCreationRequest.builder()
                                .loginMethod(PredefineLoginMethod.WALLET_METHOD)
                                .walletAddress(request.getAddress())
                                .build();
                        return userMapper.toUser(userService.createUser(createReq));
                    });

            if(user.getStatus().equals(UserStatus.BANNED))
                throw new AppException(ErrorCode.BANED);

            var token = generateToken(user);

            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();
        }


        throw new AppException(ErrorCode.LOGIN_METHOD_NOT_SUPPORTED);
    }


    public LogoutResponse logout(String token) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(token, true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
            return LogoutResponse.builder().success(true).message("Logout successful").build();
        } catch (AppException exception) {
            return LogoutResponse.builder().success(false).message("Token already expired").build();
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var userId = signedJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("c2vn-lms")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        Role role = user.getRole();

        if (role != null) {
            stringJoiner.add("ROLE_" + role.getName());
        }

        return stringJoiner.toString();
    }

}
