package com.mycom.myapp.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.MemberUpdateRequest;
import com.mycom.myapp.domain.member.dto.PasswordChangeRequest;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.dto.WithdrawRequest;
import com.mycom.myapp.domain.member.service.MemberService;
import com.mycom.myapp.global.common.dto.ResultDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(description = "USER : 회원가입")
    @PostMapping
    public ResponseEntity<ResultDto<MemberResponse>> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.signup(request);

        ResultDto<MemberResponse> result = ResultDto.<MemberResponse>builder()
                .message("회원가입이 완료되었습니다.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(description = "USER : 마이페이지 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ResultDto<MemberResponse>> getMyPage(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String username) {
        MemberResponse response = memberService.getMyPage(id, username);

        ResultDto<MemberResponse> result = ResultDto.<MemberResponse>builder()
                .message("조회 성공")
                .data(response)
                .build();

        return ResponseEntity.ok(result);
    }

    @Operation(description = "USER : 회원 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ResultDto<MemberResponse>> updateInfo(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String username,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.updateInfo(id, username, request);

        ResultDto<MemberResponse> result = ResultDto.<MemberResponse>builder()
                .message("정보가 수정되었습니다.")
                .data(response)
                .build();

        return ResponseEntity.ok(result);
    }

    @Operation(description = "USER : 비밀번호 변경")
    @PatchMapping("/{id}/password")
    public ResponseEntity<ResultDto<Void>> changePassword(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String username,
            @Valid @RequestBody PasswordChangeRequest request) {
        memberService.changePassword(id, username, request);

        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("비밀번호가 변경되었습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(result);
    }

    @Operation(description = "USER : 회원 탈퇴")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResultDto<Void>> withdraw(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String username,
            @Valid @RequestBody WithdrawRequest request) {
        memberService.withdraw(id, username, request);

        ResultDto<Void> result = ResultDto.<Void>builder()
                .message("탈퇴가 완료되었습니다.")
                .data(null)
                .build();

        return ResponseEntity.ok(result);
    }
}
