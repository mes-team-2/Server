package com.final_project.battery.controller;

import com.final_project.battery.dto.request.LoginRequestDto;
import com.final_project.battery.dto.request.PasswordChangeRequestDto;
import com.final_project.battery.dto.request.TokenRequestDto;
import com.final_project.battery.dto.response.TokenDto;
import com.final_project.battery.service.AuthService;
import com.final_project.battery.util.SecurityUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseEntity.ok(authService.reissue(tokenRequestDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout(SecurityUtil.getCurrentWorkerCode());
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequestDto dto) {
        authService.changePassword(dto);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}