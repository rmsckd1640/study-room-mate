package com.mycom.myapp.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.auth.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // 재설정 링크로 들어온 토큰 문자열로 조회
    Optional<PasswordResetToken> findByToken(String token);

    // 재요청/사용 완료 시 기존 토큰 무효화(삭제)
    void deleteByMember_Id(Long memberId);
}
