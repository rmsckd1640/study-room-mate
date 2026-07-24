package com.mycom.myapp.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

// 비밀번호 재설정 "확정" 단계 - 메일로 받은 토큰과 새 비밀번호를 입력받음
public record PasswordResetConfirmRequest(
        @NotBlank String token,
        @NotBlank String newPassword
) {
}
