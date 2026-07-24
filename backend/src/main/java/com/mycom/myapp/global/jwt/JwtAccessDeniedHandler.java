package com.mycom.myapp.global.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.mycom.myapp.global.common.dto.ResultDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

// 인증은 됐지만 권한(role)이 부족한 요청(예: USER가 ADMIN 전용 API 호출)을 403으로 응답한다.
// 이것도 필터 단계에서 걸리기 때문에 GlobalExceptionHandler가 못 잡아서 따로 만든다.
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("접근 권한이 없습니다.")
                .data(null)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
