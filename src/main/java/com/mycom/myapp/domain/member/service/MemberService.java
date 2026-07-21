package com.mycom.myapp.domain.member.service;

import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.MemberUpdateRequest;
import com.mycom.myapp.domain.member.dto.PasswordChangeRequest;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.dto.WithdrawRequest;

public interface MemberService {
    MemberResponse signup(SignupRequest request);

    MemberResponse getMyPage(Long id, String requestUsername);

    MemberResponse updateInfo(Long id, String requestUsername, MemberUpdateRequest request);

    void changePassword(Long id, String requestUsername, PasswordChangeRequest request);

    void withdraw(Long id, String requestUsername, WithdrawRequest request);
}
