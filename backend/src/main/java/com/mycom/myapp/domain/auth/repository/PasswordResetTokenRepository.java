package com.mycom.myapp.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.auth.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // 재설정 링크로 들어온 토큰 문자열로 조회
    Optional<PasswordResetToken> findByToken(String token);

    // 재요청/사용 완료 시 기존 토큰 무효화(삭제)
    void deleteByMember_Id(Long memberId);

    // 만료된 뒤에도 삭제되지 않고 남아있는 토큰을 정리하기 위한 배치용 삭제
    void deleteByExpiryDateBefore(LocalDateTime time);
}
