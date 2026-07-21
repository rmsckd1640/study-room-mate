package com.mycom.myapp.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.domain.auth.service.AuthServiceImpl;
import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.repository.MemberRepository;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.jwt.JwtProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthServiceImpl authService;

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

    @Test
    @DisplayName("아이디/비밀번호가 맞으면 로그인에 성공하고 토큰을 발급한다")
    void login_성공() {
        // given
        Member member = createMember();
        LoginRequest request = new LoginRequest("chang123", "password1234");

        given(memberRepository.findByUsername("chang123")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password1234", "encoded-password")).willReturn(true);
        given(jwtProvider.createAccessToken(1L, "chang123", MemberRole.USER)).willReturn("access-token");
        given(jwtProvider.createRefreshToken("chang123")).willReturn("refresh-token");
        given(jwtProvider.getExpiration("refresh-token")).willReturn(LocalDateTime.now().plusDays(14));

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).deleteByMember_Id(1L);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 아이디면 InvalidCredentialsException이 발생한다")
    void login_실패_아이디없음() {
        // given
        LoginRequest request = new LoginRequest("nobody", "password1234");
        given(memberRepository.findByUsername("nobody")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 InvalidCredentialsException이 발생한다")
    void login_실패_비밀번호불일치() {
        // given
        Member member = createMember();
        LoginRequest request = new LoginRequest("chang123", "wrong-password");

        given(memberRepository.findByUsername("chang123")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("로그아웃하면 해당 회원의 RefreshToken을 삭제한다")
    void logout_성공() {
        // when
        authService.logout("chang123");

        // then
        verify(refreshTokenRepository).deleteByMember_Username("chang123");
    }
}
