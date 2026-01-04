package com.cardano_lms.server.OAuth2;

import com.cardano_lms.server.constant.PredefineLoginMethod;
import com.cardano_lms.server.DTO.Request.UserCreationRequest;
import com.cardano_lms.server.DTO.Response.UserResponse;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Mapper.UserMapper;
import com.cardano_lms.server.Repository.UserRepository;
import com.cardano_lms.server.Service.AuthenticationService;
import com.cardano_lms.server.Service.GitHubEmailService;
import com.cardano_lms.server.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;
    private final GitHubEmailService gitHubEmailService;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    public CustomOAuth2SuccessHandler(
            AuthenticationService authenticationService,
            UserRepository userRepository,
            UserMapper userMapper,
            UserService userService,
            GitHubEmailService gitHubEmailService,
            @Lazy OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userService = userService;
        this.gitHubEmailService = gitHubEmailService;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Value("${FRONTEND_URL}")
    private String FRONTEND_URL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        String email = null;
        String name = null;
        String githubUsername = null;
        String googleId = null;

        if ("google".equalsIgnoreCase(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            googleId = oAuth2User.getAttribute("sub");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            githubUsername = oAuth2User.getAttribute("login");
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            if (name == null || name.isBlank()) {
                name = githubUsername;
            }

            if (email == null || email.isBlank()) {
                try {
                    OAuth2AuthorizedClient client = authorizedClientRepository.loadAuthorizedClient(
                            registrationId,
                            authentication,
                            request
                    );
                    if (client != null && client.getAccessToken() != null) {
                        String accessToken = client.getAccessToken().getTokenValue();
                        email = gitHubEmailService.getPrimaryEmail(accessToken);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if ("google".equalsIgnoreCase(registrationId)) {
            if (email == null && googleId == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        } else if ("github".equalsIgnoreCase(registrationId)) {
            if (githubUsername == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }

        if (email != null) {
            email = email.trim().toLowerCase();
        }

        User user;

        if ("google".equalsIgnoreCase(registrationId)) {
            Optional<User> byGoogle = (googleId != null) ? userRepository.findByGoogle(googleId) : Optional.empty();
            
            if (byGoogle.isPresent()) {
                user = byGoogle.get();
            } 
            else if (email != null && !email.isBlank()) {
                Optional<User> byEmail = userRepository.findByEmailIgnoreCase(email);
                if (byEmail.isPresent()) {
                    user = byEmail.get();
                    user.setGoogle(googleId);
                } else {
                    user = createNewUser(email, name, googleId, null, PredefineLoginMethod.GOOGLE_METHOD);
                }
            } else {
                user = createNewUser(null, name, googleId, null, PredefineLoginMethod.GOOGLE_METHOD);
            }
            
            updateUserInfo(user, email, name);
            user = userRepository.save(user);

        } else { 
            Optional<User> byGithub = userRepository.findByGithub(githubUsername);
            
            if (byGithub.isPresent()) {
                user = byGithub.get();
            }
            else if (email != null && !email.isBlank()) {
                Optional<User> byEmail = userRepository.findByEmailIgnoreCase(email);
                if (byEmail.isPresent()) {
                    user = byEmail.get();
                    user.setGithub(githubUsername);
                } else {
                    user = createNewUser(email, name, null, githubUsername, PredefineLoginMethod.GITHUB_METHOD);
                }
            } else {
                user = createNewUser(null, name != null ? name : githubUsername, null, githubUsername, PredefineLoginMethod.GITHUB_METHOD);
            }
            
            updateUserInfo(user, email, name);
            user = userRepository.save(user);
        }

        String token = authenticationService.generateToken(user);
        
        boolean isLocalhost = FRONTEND_URL.contains("localhost") || FRONTEND_URL.contains("127.0.0.1");
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(!isLocalhost)
                .sameSite(isLocalhost ? "Lax" : "None")
                .path("/")
                .maxAge(36000)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        response.sendRedirect(FRONTEND_URL + "/login/success");
    }

    private User createNewUser(String email, String name, String googleId, String github, String loginMethod) {
        String computedName = name;
        if (computedName == null || computedName.isBlank()) {
            if (email != null && email.contains("@")) {
                computedName = email.substring(0, email.indexOf('@'));
            } else if (github != null && !github.isBlank()) {
                computedName = github;
            } else {
                computedName = "User";
            }
        }

        UserCreationRequest.UserCreationRequestBuilder builder = UserCreationRequest.builder()
                .email(email)
                .fullName(computedName)
                .loginMethod(loginMethod);

        if (googleId != null) builder.google(googleId);
        if (github != null) builder.github(github);

        UserResponse userResponse = userService.createUser(builder.build());
        return userMapper.toUser(userResponse);
    }

    private void updateUserInfo(User user, String email, String name) {
        if ((user.getEmail() == null || user.getEmail().isBlank()) && email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if ((user.getFullName() == null || user.getFullName().isBlank()) && name != null && !name.isBlank()) {
            user.setFullName(name);
        }
    }
}
