package com.mycom.myapp.domain.member.repository;

import com.mycom.myapp.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
