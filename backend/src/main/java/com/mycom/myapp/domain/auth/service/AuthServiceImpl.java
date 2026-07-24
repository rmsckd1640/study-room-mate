package com.mycom.myapp.domain.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.dto.PasswordResetConfirmRequest;
import com.mycom.myapp.domain.auth.dto.PasswordResetRequest;
import com.mycom.myapp.domain.auth.dto.ReissueRequest;
import com.mycom.myapp.domain.auth.entity.PasswordResetToken;
import com.mycom.myapp.domain.auth.entity.RefreshToken;
import com.mycom.myapp.domain.auth.repository.PasswordResetTokenRepository;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.exception.InvalidPasswordResetTokenException;
import com.mycom.myapp.global.exception.InvalidRefreshTokenException;
import com.mycom.myapp.global.jwt.JwtProvider;
import com.mycom.myapp.global.mail.MailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // 비밀번호 재설정 링크의 유효 시간
    private static final long PASSWORD_RESET_EXPIRATION_MINUTES = 30;

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MailService mailService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 아이디 없음/비밀번호 틀림을 같은 예외·메시지로 응답 (계정 열거 공격 방지)
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 탈퇴한 회원도 같은 메시지로 응답 (계정 열거 공격 방지 원칙을 동일하게 적용)
        if (member.getDeletedAt() != null) {
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

    // 비밀번호 재설정 "요청" 단계: 토큰 발급 + DB 저장 + 메일 발송
    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        // 존재하지 않는 이메일/탈퇴한 회원이어도 응답은 컨트롤러에서 항상 동일하게 나감 (계정 열거 공격 방지)
        memberRepository.findByEmail(request.email())
                .filter(member -> member.getDeletedAt() == null)
                .ifPresent(member -> {
                    String token = UUID.randomUUID().toString();
                    LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(PASSWORD_RESET_EXPIRATION_MINUTES);

                    // 재요청 시 이전 토큰은 무효화
                    passwordResetTokenRepository.deleteByMember_Id(member.getId());
                    passwordResetTokenRepository.save(PasswordResetToken.builder()
                            .member(member)
                            .token(token)
                            .expiryDate(expiryDate)
                            .build());

                    mailService.sendPasswordResetEmail(member.getEmail(), token);
                });
    }

    // 비밀번호 재설정 "확정" 단계: 토큰 검증 후 비밀번호 변경
    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidPasswordResetTokenException("유효하지 않은 요청입니다. 다시 시도해주세요."));

        if (resetToken.isExpired()) {
            throw new InvalidPasswordResetTokenException("유효하지 않은 요청입니다. 다시 시도해주세요.");
        }

        Member member = resetToken.getMember();
        member.changePassword(passwordEncoder.encode(request.newPassword()));

        // 1회용 - 사용한 토큰은 즉시 삭제
        passwordResetTokenRepository.deleteByMember_Id(member.getId());
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
