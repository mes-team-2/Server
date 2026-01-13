package com.final_project.battery.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.final_project.battery.exception.ErrorCode;
import com.final_project.battery.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // 필요한 권한이 없이 접근하려 할 때 403
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String result = objectMapper.writeValueAsString(
                ErrorResponse.builder()
                        .status(ErrorCode.ACCESS_DENIED.getStatus().value())
                        .error(ErrorCode.ACCESS_DENIED.getStatus().name())
                        .code(ErrorCode.ACCESS_DENIED.name())
                        .message(ErrorCode.ACCESS_DENIED.getMessage())
                        .build()
        );

        response.getWriter().write(result);
    }
}