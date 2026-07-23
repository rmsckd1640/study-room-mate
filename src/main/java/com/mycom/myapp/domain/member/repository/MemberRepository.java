package com.mycom.myapp.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Member> findByUsername(String username);

    // 아이디 찾기 - 이름+이메일 일치 확인
    Optional<Member> findByNameAndEmail(String name, String email);

    // 비밀번호 재설정 요청 - 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // 정보 수정 시 "본인의 기존 이메일"은 중복으로 치지 않기 위해 본인 id는 제외하고 검사
    boolean existsByEmailAndIdNot(String email, Long id);
}
