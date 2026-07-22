package com.mycom.myapp.domain.member.dto;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberGrade;
import com.mycom.myapp.domain.member.entity.MemberRole;

public record MemberResponse(
        Long id,
        String username,
        String email,
        String name,
        MemberRole role,
        MemberGrade grade
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getGrade()
        );
    }
}
