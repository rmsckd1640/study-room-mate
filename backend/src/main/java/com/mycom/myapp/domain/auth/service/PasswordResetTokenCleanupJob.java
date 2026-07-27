package com.mycom.myapp.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.domain.auth.repository.PasswordResetTokenRepository;

import lombok.RequiredArgsConstructor;

// 만료됐지만(30분 경과) 삭제되지는 않아 계속 쌓이는 PasswordResetToken을 주기적으로 청소한다.
// 정상 흐름(재요청/비밀번호 변경 완료)에서는 이미 삭제되므로, 여기 걸리는 건 사용자가 링크를
// 끝까지 쓰지 않고 방치한 토큰뿐이다.
@Component
@RequiredArgsConstructor
public class PasswordResetTokenCleanupJob {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void deleteExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
