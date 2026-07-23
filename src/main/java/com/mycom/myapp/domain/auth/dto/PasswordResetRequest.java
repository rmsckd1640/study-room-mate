package com.mycom.myapp.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 비밀번호 재설정 "요청" 단계 - 재설정 링크를 받을 이메일만 입력받음
public record PasswordResetRequest(
        @NotBlank @Email String email
) {
}
