package com.mycom.myapp.domain.member.service;

import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.SignupRequest;

public interface MemberService {
    MemberResponse signup(SignupRequest request);
}
