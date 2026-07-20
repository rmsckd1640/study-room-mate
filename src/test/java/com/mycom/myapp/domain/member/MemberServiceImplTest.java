package com.mycom.myapp.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.member.service.MemberServiceImpl;
import com.mycom.myapp.global.exception.DuplicateEmailException;
import com.mycom.myapp.global.exception.DuplicateUsernameException;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private SignupRequest createRequest() {
        return new SignupRequest("chang123", "password1234", "chang@test.com", "창");
    }

    @Test
    @DisplayName("정상 요청이면 회원가입에 성공한다")
    void signup_성공() {
        // given
        SignupRequest request = createRequest();
        given(memberRepository.existsByUsername("chang123")).willReturn(false);
        given(memberRepository.existsByEmail("chang@test.com")).willReturn(false);
        given(passwordEncoder.encode("password1234")).willReturn("encoded-password");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        // when
        MemberResponse response = memberService.signup(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("chang123");
        assertThat(response.email()).isEqualTo("chang@test.com");
        assertThat(response.name()).isEqualTo("창");
        assertThat(response.role()).isEqualTo(MemberRole.USER);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getPassword()).isEqualTo("encoded-password");
    }

    @Test
    @DisplayName("아이디가 중복이면 DuplicateUsernameException이 발생한다")
    void signup_실패_아이디중복() {
        // given
        SignupRequest request = createRequest();
        given(memberRepository.existsByUsername("chang123")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signup(request))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일이 중복이면 DuplicateEmailException이 발생한다")
    void signup_실패_이메일중복() {
        // given
        SignupRequest request = createRequest();
        given(memberRepository.existsByUsername("chang123")).willReturn(false);
        given(memberRepository.existsByEmail("chang@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signup(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(memberRepository, never()).save(any());
    }
}
