package com.final_project.battery.config;

import com.final_project.battery.jwt.JwtAccessDeniedHandler;
import com.final_project.battery.jwt.JwtAuthenticationEntryPoint;
import com.final_project.battery.jwt.JwtFilter;
import com.final_project.battery.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable())
                .headers((headers) -> headers.frameOptions((frame) -> frame.sameOrigin()))

                // 세션 미사용
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리 핸들러 등록
                .exceptionHandling((handling) -> handling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler))          // 403

                // 권한 설정
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/auth/login", "/auth/reissue").permitAll()
                                .requestMatchers("/api/machines/**").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/api/bom/**").authenticated()
                                .requestMatchers("/api/master/**").authenticated()
                                .requestMatchers("/api/dashboard").authenticated()
                        .requestMatchers("/auth/logout").authenticated()
                                .requestMatchers("/api/log/**").permitAll()
                                .requestMatchers("/api/defect-logs").authenticated()
                                .requestMatchers("/api/defect-logs").permitAll()
                                .requestMatchers("/api/product-lots").authenticated()
                                .requestMatchers("/api/product-lots/**").authenticated()
                                .requestMatchers("/api/shipments").authenticated()
                                .requestMatchers("/api/shipments").permitAll()




                        // 아래는 Swagger 건들 ㄴ.ㄴ
                        .requestMatchers(
                                "/v3/api-docs/**",
                                         "/swagger-ui/**",
                                         "/swagger-ui.html").permitAll()
//                         .requestMatchers("/api/~~/**").hasAuthority("ADMIN") // 관리자 예시
                        .anyRequest().authenticated()
                )

                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}