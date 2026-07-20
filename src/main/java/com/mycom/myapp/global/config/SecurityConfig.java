package com.mycom.myapp.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.mycom.myapp.global.jwt.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 매 요청마다 Access Token을 검사해서 SecurityContext를 채워주는 필터 (주입받음)
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // JWT는 쿠키/세션이 아니라 헤더로 토큰을 주고받아서 CSRF 공격 경로 자체가 없음 -> 끔
            .csrf(AbstractHttpConfigurer::disable)
            // 우리가 직접 로그인 API로 JWT를 발급할 거라 Spring 기본 로그인 폼/HTTP Basic은 불필요 -> 끔
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // 서버가 세션을 만들지도, 기억하지도 않는다 (JWT 자체가 인증 정보를 담고 있으므로)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 회원가입: 아직 인증 전 단계이므로 누구나 호출 가능해야 함
                .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                // 로그인: 마찬가지로 토큰이 생기기 전 단계라 인증 없이 호출 가능해야 함
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // 그 외 모든 요청은 인증(유효한 Access Token)이 있어야 함
                .anyRequest().authenticated()
            )
            // JwtAuthFilter를 UsernamePasswordAuthenticationFilter보다 먼저 실행시켜서
            // 다른 인증 방식이 시도되기 전에 우리 JWT 검사가 먼저 SecurityContext를 채우게 함
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
