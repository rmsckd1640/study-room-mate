package com.mycom.myapp.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.MemberUpdateRequest;
import com.mycom.myapp.domain.member.dto.PasswordChangeRequest;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.dto.WithdrawRequest;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.domain.member.service.MemberServiceImpl;
import com.mycom.myapp.global.exception.DuplicateEmailException;
import com.mycom.myapp.global.exception.DuplicateUsernameException;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member createMember() {
        Member member = Member.builder()
                .username("chang123")
                .password("encoded-password")
                .email("chang@test.com")
                .name("창")
                .role(MemberRole.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }

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

    @Test
    @DisplayName("본인이 조회하면 마이페이지 정보를 반환한다")
    void getMyPage_성공() {
        // given
        Member member = createMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getMyPage(1L, "chang123");

        // then
        assertThat(response.username()).isEqualTo("chang123");
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 UserNotFoundException이 발생한다")
    void getMyPage_실패_존재하지않는회원() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMyPage(999L, "chang123"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("본인이 아니면 AccessDeniedException이 발생한다")
    void getMyPage_실패_본인아님() {
        // given
        Member member = createMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> memberService.getMyPage(1L, "other_user"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("정보 수정에 성공하면 이름과 이메일이 바뀐다")
    void updateInfo_성공() {
        // given
        Member member = createMember();
        MemberUpdateRequest request = new MemberUpdateRequest("새이름", "new@test.com");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByEmailAndIdNot("new@test.com", 1L)).willReturn(false);

        // when
        MemberResponse response = memberService.updateInfo(1L, "chang123", request);

        // then
        assertThat(response.name()).isEqualTo("새이름");
        assertThat(response.email()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("본인의 기존 이메일 그대로 제출하면 중복 검사 자체를 하지 않는다")
    void updateInfo_성공_기존이메일유지() {
        // given
        Member member = createMember();
        MemberUpdateRequest request = new MemberUpdateRequest("새이름", "chang@test.com");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.updateInfo(1L, "chang123", request);

        // then
        assertThat(response.name()).isEqualTo("새이름");
        verify(memberRepository, never()).existsByEmailAndIdNot(any(), any());
    }

    @Test
    @DisplayName("다른 회원이 쓰는 이메일로 변경하면 DuplicateEmailException이 발생한다")
    void updateInfo_실패_이메일중복() {
        // given
        Member member = createMember();
        MemberUpdateRequest request = new MemberUpdateRequest("새이름", "taken@test.com");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByEmailAndIdNot("taken@test.com", 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.updateInfo(1L, "chang123", request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("현재 비밀번호가 맞으면 비밀번호가 변경된다")
    void changePassword_성공() {
        // given
        Member member = createMember();
        PasswordChangeRequest request = new PasswordChangeRequest("password1234", "newpassword5678");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password1234", "encoded-password")).willReturn(true);
        given(passwordEncoder.encode("newpassword5678")).willReturn("new-encoded-password");

        // when
        memberService.changePassword(1L, "chang123", request);

        // then
        assertThat(member.getPassword()).isEqualTo("new-encoded-password");
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 InvalidCredentialsException이 발생한다")
    void changePassword_실패_비밀번호불일치() {
        // given
        Member member = createMember();
        PasswordChangeRequest request = new PasswordChangeRequest("wrong-password", "newpassword5678");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.changePassword(1L, "chang123", request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("비밀번호가 맞으면 탈퇴 처리되고 RefreshToken도 삭제된다")
    void withdraw_성공() {
        // given
        Member member = createMember();
        WithdrawRequest request = new WithdrawRequest("password1234");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password1234", "encoded-password")).willReturn(true);

        // when
        memberService.withdraw(1L, "chang123", request);

        // then
        assertThat(member.getDeletedAt()).isNotNull();
        verify(refreshTokenRepository).deleteByMember_Username("chang123");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 탈퇴가 거부되고 세션도 그대로 유지된다")
    void withdraw_실패_비밀번호불일치() {
        // given
        Member member = createMember();
        WithdrawRequest request = new WithdrawRequest("wrong-password");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(1L, "chang123", request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).deleteByMember_Username(any());
    }

    @Test
    @DisplayName("관리자는 전체 회원 목록을 조회한다")
    void getAllMembers_성공() {
        // given
        given(memberRepository.findAll()).willReturn(List.of(createMember()));

        // when
        List<MemberResponse> response = memberService.getAllMembers();

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).username()).isEqualTo("chang123");
    }

    @Test
    @DisplayName("관리자는 소유권 검사 없이 회원 단건을 조회한다")
    void getMember_성공() {
        // given
        Member member = createMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getMember(1L);

        // then
        assertThat(response.username()).isEqualTo("chang123");
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 UserNotFoundException이 발생한다")
    void getMember_실패_존재하지않는회원() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMember(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("관리자는 비밀번호 확인 없이 회원을 강제 탈퇴시킨다")
    void adminWithdraw_성공() {
        // given
        Member member = createMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        memberService.adminWithdraw(1L);

        // then
        assertThat(member.getDeletedAt()).isNotNull();
        verify(refreshTokenRepository).deleteByMember_Username("chang123");
    }
}
