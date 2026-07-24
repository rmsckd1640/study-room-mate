package com.mycom.myapp.global.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.mycom.myapp.global.common.dto.ResultDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

// 인증 자체가 안 된 요청(토큰 없음/만료/위조)이 보호된 API에 접근하면 여기서 401로 응답한다.
// Controller까지 도달하지 못하고 필터 단계에서 막힌 요청이라 GlobalExceptionHandler가 못 잡아서 따로 만든다.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("인증이 필요합니다.")
                .data(null)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
