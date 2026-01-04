package com.cardano_lms.server.Config;

import com.cardano_lms.server.OAuth2.CustomOAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
        private final String[] PUBLIC_ENDPOINTS = {
                        "/api/users",
                        "/api/auth/token",
                        "/api/auth/introspect",
                        "/api/auth/logout",
                        "/api/auth/refresh",
                        "/api/nonce",
                        "/api/auth/register",
                        "/api/auth/verify-email",
                        "/api/auth/resend-code",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password"
        };
        private final String[] PUBLIC_GET_ENDPOINTS = {
                        "/health",
                        "/api/media",
                        "/api/tags",
                        "/api/course/search",
                        "/api/course/**",
                        "/api/instructor-profiles/**",
                        "/api/search/**",
                        "/api/feedbacks/course/**",
                        "/api/face/test-connection",
                        "/api/certificates/verify",
                        "/api/certificates/verify/**",
                        
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",

        };

        private final CustomJwtDecoder customJwtDecoder;
        private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
        private final JwtCookieFilter jwtCookieFilter;
        private final ClientRegistrationRepository clientRegistrationRepository;

        public SecurityConfig(@Lazy CustomJwtDecoder customJwtDecoder, 
                              @Lazy @Autowired(required = false) CustomOAuth2SuccessHandler customOAuth2SuccessHandler, 
                              JwtCookieFilter jwtCookieFilter,
                              @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
                this.customJwtDecoder = customJwtDecoder;
                this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
                this.jwtCookieFilter = jwtCookieFilter;
                this.clientRegistrationRepository = clientRegistrationRepository;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                        JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
                httpSecurity
                                .cors(Customizer.withDefaults())
                                .addFilterBefore(jwtCookieFilter, BearerTokenAuthenticationFilter.class)
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                                                
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html",
                                                                "/swagger-ui/**")
                                                .permitAll()
                                                .requestMatchers("/ws/**").permitAll()
                                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                                                .anyRequest().authenticated());

                if (clientRegistrationRepository != null && customOAuth2SuccessHandler != null) {
                        httpSecurity.oauth2Login(oauth2 -> oauth2
                                        .successHandler(customOAuth2SuccessHandler));
                }
                
                httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwtConfigurer -> jwtConfigurer
                                                .decoder(customJwtDecoder)
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

                httpSecurity.csrf(AbstractHttpConfigurer::disable);

                return httpSecurity.build();
        }

        @Bean
        public CorsFilter corsFilter() {
                CorsConfiguration corsConfiguration = new CorsConfiguration();

                corsConfiguration.setAllowCredentials(true);
                corsConfiguration.addAllowedOrigin("http://localhost:3000");
                corsConfiguration.addAllowedOrigin("https://7nwmd6xj-3000.asse.devtunnels.ms");
                corsConfiguration.addAllowedOrigin("https://lms-eosin-five.vercel.app");
                corsConfiguration.addAllowedOrigin("https://lms.lab3.io.vn");
                
                corsConfiguration.addAllowedOriginPattern("https://*.vercel.app");
                corsConfiguration.addAllowedOriginPattern("https://*.lab3.io.vn");
                corsConfiguration.addAllowedMethod("*");
                corsConfiguration.addAllowedHeader("*");
                corsConfiguration.addExposedHeader("Authorization");

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfiguration);

                return new CorsFilter(source);
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

                return jwtAuthenticationConverter;
        }

}
