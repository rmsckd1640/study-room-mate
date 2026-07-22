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
import com.mycom.myapp.domain.auth.dto.PasswordResetConfirmRequest;
import com.mycom.myapp.domain.auth.dto.PasswordResetRequest;
import com.mycom.myapp.domain.auth.dto.ReissueRequest;
import com.mycom.myapp.domain.auth.service.AuthService;
import com.mycom.myapp.domain.member.dto.FindUsernameRequest;
import com.mycom.myapp.domain.member.service.MemberService;
import com.mycom.myapp.global.common.dto.ResultDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    // 아이디 찾기는 Auth 도메인의 관심사가 아니라 순수 Member 조회라 MemberService를 직접 사용
    private final MemberService memberService;

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

    @PostMapping("/find-username")
    public ResponseEntity<ResultDto<String>> findUsername(@Valid @RequestBody FindUsernameRequest request) {
        String username = memberService.findUsername(request);

        ResultDto<String> result = ResultDto.<String>builder()
                .message("아이디 조회 성공")
                .data(username)
                .build();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ResultDto<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);

        // 이메일 존재 여부와 무관하게 항상 동일한 응답 (계정 열거 공격 방지)
        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("입력하신 이메일로 재설정 링크를 보냈습니다. 메일함을 확인해주세요.")
                .data(null)
                .build();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ResultDto<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);

        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("비밀번호가 재설정되었습니다.")
                .data(null)
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
