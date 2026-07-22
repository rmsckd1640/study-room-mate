package com.mycom.myapp.domain.member.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.MemberUpdateRequest;
import com.mycom.myapp.domain.member.dto.PasswordChangeRequest;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.dto.WithdrawRequest;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.global.exception.DuplicateEmailException;
import com.mycom.myapp.global.exception.DuplicateUsernameException;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .name(request.name())
                .build();

        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    @Override
    public MemberResponse getMyPage(Long id, String requestUsername) {
        Member member = findMemberOrThrow(id);
        validateOwner(member, requestUsername);

        return MemberResponse.from(member);
    }

    @Override
    @Transactional
    public MemberResponse updateInfo(Long id, String requestUsername, MemberUpdateRequest request) {
        Member member = findMemberOrThrow(id);
        validateOwner(member, requestUsername);

        // 본인의 기존 이메일 그대로 제출한 경우는 중복 검사 대상에서 제외
        if (!member.getEmail().equals(request.email())
                && memberRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        member.updateInfo(request.name(), request.email());

        return MemberResponse.from(member);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String requestUsername, PasswordChangeRequest request) {
        Member member = findMemberOrThrow(id);
        validateOwner(member, requestUsername);

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional
    public void withdraw(Long id, String requestUsername, WithdrawRequest request) {
        Member member = findMemberOrThrow(id);
        validateOwner(member, requestUsername);

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        member.delete();
        // 탈퇴하면 로그인 상태도 같이 끊어야 하므로 Refresh Token도 정리
        refreshTokenRepository.deleteByMember_Username(requestUsername);
    }

    @Override
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Override
    public MemberResponse getMember(Long id) {
        return MemberResponse.from(findMemberOrThrow(id));
    }

    @Override
    @Transactional
    public void adminWithdraw(Long id) {
        Member member = findMemberOrThrow(id);

        member.delete();
        refreshTokenRepository.deleteByMember_Username(member.getUsername());
    }

    private Member findMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));
    }

    // 요청을 보낸 사람(토큰의 username)과 대상 회원이 같은지 확인 - 다르면 403
    private void validateOwner(Member member, String requestUsername) {
        if (!member.getUsername().equals(requestUsername)) {
            throw new AccessDeniedException("본인만 접근할 수 있습니다.");
        }
    }
}
