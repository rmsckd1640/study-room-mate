package com.mycom.myapp.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
