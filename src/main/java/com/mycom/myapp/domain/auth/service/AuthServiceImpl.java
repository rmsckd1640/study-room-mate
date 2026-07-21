package com.mycom.myapp.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.entity.RefreshToken;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 아이디 없음/비밀번호 틀림을 같은 예외·메시지로 응답 (계정 열거 공격 방지)
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getUsername(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getUsername());
        LocalDateTime expiryDate = jwtProvider.getExpiration(refreshToken);

        // 회원당 세션 1개로 관리: 기존 Refresh Token이 있으면 지우고 새로 저장
        refreshTokenRepository.deleteByMember_Id(member.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiryDate(expiryDate)
                .build());

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(String username) {
        refreshTokenRepository.deleteByMember_Username(username);
    }
}
