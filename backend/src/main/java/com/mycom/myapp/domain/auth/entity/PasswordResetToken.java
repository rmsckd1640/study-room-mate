package com.mycom.myapp.domain.auth.entity;

import java.time.LocalDateTime;

import com.mycom.myapp.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 토큰이 어느 회원의 비밀번호 재설정 요청인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 재설정 링크에 실어 보내는 무작위 문자열 (UUID)
    @Column(nullable = false)
    private String token;

    // 이 시각이 지나면 더 이상 이 토큰으로 비밀번호를 바꿀 수 없음
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder
    public PasswordResetToken(Member member, String token, LocalDateTime expiryDate) {
        this.member = member;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    // 현재 시각이 만료시각을 지났는지 여부 - DB에 남아있어도 만료된 토큰은 사용 불가 처리하기 위함
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
