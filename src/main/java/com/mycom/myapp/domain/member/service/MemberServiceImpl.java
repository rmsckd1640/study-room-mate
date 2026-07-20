package com.mycom.myapp.domain.member.service;

import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.global.exception.DuplicateEmailException;
import com.mycom.myapp.global.exception.DuplicateUsernameException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
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
}
