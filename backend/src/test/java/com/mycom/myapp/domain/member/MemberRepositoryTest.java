package com.mycom.myapp.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.repository.MemberRepository;

@DataJpaTest
@EnableJpaRepositories(basePackageClasses = MemberRepository.class)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member createMember(String username, String email) {
        return Member.builder()
                .username(username)
                .password("encoded-password")
                .email(email)
                .name("테스트")
                .build();
    }

    @Test
    @DisplayName("존재하는 username이면 true를 반환한다")
    void existsByUsername_존재함() {
        memberRepository.save(createMember("chang123", "chang@test.com"));

        boolean result = memberRepository.existsByUsername("chang123");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 username이면 false를 반환한다")
    void existsByUsername_존재하지_않음() {
        boolean result = memberRepository.existsByUsername("nobody");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("존재하는 email이면 true를 반환한다")
    void existsByEmail_존재함() {
        memberRepository.save(createMember("chang123", "chang@test.com"));

        boolean result = memberRepository.existsByEmail("chang@test.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 email이면 false를 반환한다")
    void existsByEmail_존재하지_않음() {
        boolean result = memberRepository.existsByEmail("nobody@test.com");

        assertThat(result).isFalse();
    }
}
