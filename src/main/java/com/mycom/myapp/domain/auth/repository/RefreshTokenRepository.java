package com.mycom.myapp.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByMember_Id(Long memberId);

    void deleteByMember_Username(String username);
}
