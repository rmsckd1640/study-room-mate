package com.mycom.myapp.domain.member.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.service.MemberService;
import com.mycom.myapp.global.common.dto.ResultDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminMemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ResultDto<List<MemberResponse>>> getAllMembers() {
        List<MemberResponse> response = memberService.getAllMembers();

        ResultDto<List<MemberResponse>> result = ResultDto.<List<MemberResponse>>builder()
                .message("조회 성공")
                .data(response)
                .build();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultDto<MemberResponse>> getMember(@PathVariable Long id) {
        MemberResponse response = memberService.getMember(id);

        ResultDto<MemberResponse> result = ResultDto.<MemberResponse>builder()
                .message("조회 성공")
                .data(response)
                .build();

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResultDto<Void>> withdrawMember(@PathVariable Long id) {
        memberService.adminWithdraw(id);

        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("탈퇴 처리되었습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(result);
    }
}
