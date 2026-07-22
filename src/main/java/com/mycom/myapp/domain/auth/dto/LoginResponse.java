package com.mycom.myapp.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
