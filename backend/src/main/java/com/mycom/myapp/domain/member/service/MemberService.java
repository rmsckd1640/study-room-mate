package com.mycom.myapp.domain.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.domain.member.dto.FindUsernameRequest;
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

    // 비로그인 상태에서 본인 확인 후 아이디 반환
    String findUsername(FindUsernameRequest request);

    // 관리자 전용 - 본인 확인(소유권 검사) 없이 전체/단건 조회, 강제 탈퇴
    Page<MemberResponse> getAllMembers(Pageable pageable);

    MemberResponse getMember(Long id);

    void adminWithdraw(Long id);
}
