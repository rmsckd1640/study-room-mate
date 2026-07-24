package com.mycom.myapp.global.exception;

import org.springframework.http.HttpStatus;

// 아이디 찾기 등에서, 조회된 회원이 이미 탈퇴 처리된 경우
public class WithdrawnMemberException extends BaseException {
    public WithdrawnMemberException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
