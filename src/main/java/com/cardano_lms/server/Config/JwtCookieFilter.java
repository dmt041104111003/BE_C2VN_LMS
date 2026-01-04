package com.cardano_lms.server.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    request = new HttpServletRequestWrapper(request) {
                        @Override
                        public String getHeader(String name) {
                            if ("Authorization".equals(name)) {
                                return "Bearer " + token;
                            }
                            return super.getHeader(name);
                        }
                    };
                    break;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
