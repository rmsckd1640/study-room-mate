package com.mycom.myapp.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mycom.myapp.global.jwt.JwtAccessDeniedHandler;
import com.mycom.myapp.global.jwt.JwtAuthFilter;
import com.mycom.myapp.global.jwt.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
// @PreAuthorize를 컨트롤러/서비스 메서드에 붙여서 쓸 수 있게 활성화 (안 켜면 어노테이션이 그냥 무시됨)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 매 요청마다 Access Token을 검사해서 SecurityContext를 채워주는 필터 (주입받음)
    private final JwtAuthFilter jwtAuthFilter;
    // 인증 자체가 안 된 요청을 401로 응답
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    // 인증은 됐지만 권한이 부족한 요청을 403으로 응답
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Vite 프론트 개발 서버 주소만 허용
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        // Authorization(JWT), Content-Type 등 요청 헤더 전부 허용
        config.setAllowedHeaders(List.of("*"));
        // JWT를 쿠키가 아니라 헤더로 주고받으므로 자격 증명(쿠키) 공유는 불필요
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 위에서 정의한 CORS 설정을 필터 체인에 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                // 아이디 찾기/비밀번호 재설정: 로그인 자체가 안 되는 상황을 위한 기능이라 인증 없이 호출 가능해야 함
                .requestMatchers(HttpMethod.POST, "/api/auth/find-username").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/password-reset/request", "/api/auth/password-reset/confirm").permitAll()
                // 재발급: 호출 시점엔 Access Token이 이미 만료된 상태라 인증 자체가 불가능함.
                // 대신 컨트롤러 내부에서 Refresh Token 자체를 별도로 검증한다.
                .requestMatchers(HttpMethod.POST, "/api/auth/reissue").permitAll()
                .requestMatchers(HttpMethod.GET, 
                		"/v3/api-docs/**",
                		"/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html",
                		"/favicon.ico", "/swagger-resources/**"
        		).permitAll()
                // 그 외 모든 요청은 인증(유효한 Access Token)이 있어야 함
                .anyRequest().authenticated()
            )
            // JwtAuthFilter를 UsernamePasswordAuthenticationFilter보다 먼저 실행시켜서
            // 다른 인증 방식이 시도되기 전에 우리 JWT 검사가 먼저 SecurityContext를 채우게 함
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // 필터 단계에서 걸러진 인증/인가 실패는 GlobalExceptionHandler가 아니라 여기서 처리됨
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            );

        return http.build();
    }
}
