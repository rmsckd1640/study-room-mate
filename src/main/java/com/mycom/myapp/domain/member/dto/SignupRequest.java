package com.mycom.myapp.domain.member.dto;

public record SignupRequest(
        String username,
        String password,
        String email,
        String name
) {
}
