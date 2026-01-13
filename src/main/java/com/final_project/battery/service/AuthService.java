package com.final_project.battery.service;

import com.final_project.battery.domain.RefreshToken;
import com.final_project.battery.dto.request.LoginRequestDto;
import com.final_project.battery.dto.request.TokenRequestDto;
import com.final_project.battery.dto.response.TokenDto;
import com.final_project.battery.exception.CustomException;
import com.final_project.battery.exception.ErrorCode;
import com.final_project.battery.jwt.JwtTokenProvider;
import com.final_project.battery.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenDto login(LoginRequestDto loginRequestDto) {
        // 1. Login WorkerCode/PassWord 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getWorkerCode(), loginRequestDto.getPassword());

        // 2. 실제 검증
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. Access Token 에서 Worker ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장소에서 Worker ID 를 기반으로 Refresh Token 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN)); // 로그아웃 등으로 사라진 경우

        // 4. Refresh Token 일치하는지 검사
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN); // 토큰이 다른 경우
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 6. 저장소 정보 업데이트
        refreshToken.updateValue(tokenDto.getRefreshToken());

        return tokenDto;
    }

    // 로그아웃
    @Transactional
    public void logout(String workerCode) {
        RefreshToken refreshToken = refreshTokenRepository.findByKey(workerCode)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKER_NOT_FOUND));

        refreshTokenRepository.delete(refreshToken); // DB에서 삭제
    }
}