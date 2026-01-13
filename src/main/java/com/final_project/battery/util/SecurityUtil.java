package com.final_project.battery.util;

import com.final_project.battery.exception.CustomException;
import com.final_project.battery.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() { }

    // 현재 로그인한 사용자의 workerCode(사번) 반환
    public static String getCurrentWorkerCode() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        return authentication.getName();
    }
}