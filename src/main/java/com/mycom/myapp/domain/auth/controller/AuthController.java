package com.mycom.myapp.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.dto.ReissueRequest;
import com.mycom.myapp.domain.auth.service.AuthService;
import com.mycom.myapp.global.common.dto.ResultDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResultDto<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        ResultDto<LoginResponse> result = ResultDto.<LoginResponse>builder()
                .message("로그인에 성공했습니다.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ResultDto<LoginResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        LoginResponse response = authService.reissue(request);

        ResultDto<LoginResponse> result = ResultDto.<LoginResponse>builder()
                .message("토큰이 재발급되었습니다.")
                .data(response)
                .build();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResultDto<Void>> logout(@AuthenticationPrincipal String username) {
        // JwtAuthFilter가 SecurityContext에 넣어둔 principal(username)을 직접 꺼냄
        authService.logout(username);

        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("로그아웃되었습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(result);
    }
}
