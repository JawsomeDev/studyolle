package com.studyolle.infra.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 이전 요청 URL 가져오기
        String redirectUrl = (String) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        if (redirectUrl != null) {
            request.getSession().removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            response.sendRedirect(redirectUrl); // 이전 URL로 리다이렉트
        } else {
            response.sendRedirect("/"); // 기본 URL로 리다이렉트
        }
    }
}
