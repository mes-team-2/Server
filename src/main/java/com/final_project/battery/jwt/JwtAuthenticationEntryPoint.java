package com.final_project.battery.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.final_project.battery.exception.ErrorCode;
import com.final_project.battery.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 유효한 자격증명을 제공하지 않고 접근하려 할 때 401
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 우리가 만든 ErrorResponse 형식으로 JSON 변환
        String result = objectMapper.writeValueAsString(
                ErrorResponse.builder()
                        .status(ErrorCode.INVALID_TOKEN.getStatus().value())
                        .error(ErrorCode.INVALID_TOKEN.getStatus().name())
                        .code(ErrorCode.INVALID_TOKEN.name())
                        .message("인증 정보가 없거나 유효하지 않습니다.")
                        .build()
        );

        response.getWriter().write(result);
    }
}