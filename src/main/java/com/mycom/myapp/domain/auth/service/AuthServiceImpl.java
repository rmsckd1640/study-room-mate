package com.mycom.myapp.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.dto.ReissueRequest;
import com.mycom.myapp.domain.auth.entity.RefreshToken;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.exception.InvalidRefreshTokenException;
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

        return issueTokens(member);
    }

    @Override
    @Transactional
    public void logout(String username) {
        refreshTokenRepository.deleteByMember_Username(username);
    }

    @Override
    @Transactional
    public LoginResponse reissue(ReissueRequest request) {
        String presentedToken = request.refreshToken();

        if (!jwtProvider.isValid(presentedToken)) {
            throw new InvalidRefreshTokenException("유효하지 않은 토큰입니다. 다시 로그인해주세요.");
        }

        String username = jwtProvider.getUsername(presentedToken);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 토큰입니다. 다시 로그인해주세요."));

        RefreshToken savedToken = refreshTokenRepository.findByMember_Id(member.getId())
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 토큰입니다. 다시 로그인해주세요."));

        // DB에 저장된 최신 토큰과 일치하는지 확인 (이미 교체되어 버려진 옛날 토큰의 재사용을 차단)
        if (!savedToken.getToken().equals(presentedToken)) {
            throw new InvalidRefreshTokenException("유효하지 않은 토큰입니다. 다시 로그인해주세요.");
        }

        return issueTokens(member);
    }

    // 로그인/재발급 공통 로직: 토큰 두 개를 새로 발급하고, Refresh Token은 DB에서 교체(Rotation)한다
    private LoginResponse issueTokens(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getUsername(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getUsername());
        LocalDateTime expiryDate = jwtProvider.getExpiration(refreshToken);

        refreshTokenRepository.deleteByMember_Id(member.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiryDate(expiryDate)
                .build());

        return new LoginResponse(accessToken, refreshToken);
    }
}
