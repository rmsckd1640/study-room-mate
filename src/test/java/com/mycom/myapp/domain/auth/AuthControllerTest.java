package com.mycom.myapp.domain.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.auth.controller.AuthController;
import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import com.mycom.myapp.domain.auth.dto.PasswordResetConfirmRequest;
import com.mycom.myapp.domain.auth.dto.PasswordResetRequest;
import com.mycom.myapp.domain.auth.dto.ReissueRequest;
import com.mycom.myapp.domain.auth.service.AuthService;
import com.mycom.myapp.domain.member.dto.FindUsernameRequest;
import com.mycom.myapp.domain.member.service.MemberService;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.exception.InvalidPasswordResetTokenException;
import com.mycom.myapp.global.exception.InvalidRefreshTokenException;
import com.mycom.myapp.global.exception.UserNotFoundException;
import com.mycom.myapp.global.exception.WithdrawnMemberException;
import com.mycom.myapp.global.jwt.JwtAccessDeniedHandler;
import com.mycom.myapp.global.jwt.JwtAuthFilter;
import com.mycom.myapp.global.jwt.JwtAuthenticationEntryPoint;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MemberService memberService;

    // SecurityConfig 생성자가 요구하는 빈들 - addFilters=false라 실제로 호출되진 않고,
    // ApplicationContext 로딩(SecurityConfig 생성)을 위해서만 채워준다
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    @DisplayName("아이디/비밀번호가 맞으면 200과 토큰을 반환한다")
    void login_성공() throws Exception {
        // given
        LoginRequest request = new LoginRequest("chang123", "password1234");
        LoginResponse response = new LoginResponse("access-token", "refresh-token");
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("필수 값이 빠진 채로 요청하면 400을 반환한다")
    void login_실패_검증오류() throws Exception {
        // given - username이 빈 문자열인 잘못된 요청
        String invalidRequestJson = """
                {
                    "username": "",
                    "password": "password1234"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("아이디/비밀번호가 틀리면 401을 반환한다")
    void login_실패_인증실패() throws Exception {
        // given
        LoginRequest request = new LoginRequest("chang123", "wrong-password");
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new InvalidCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("유효한 RefreshToken이면 200과 새 토큰을 반환한다")
    void reissue_성공() throws Exception {
        // given
        ReissueRequest request = new ReissueRequest("old-refresh-token");
        LoginResponse response = new LoginResponse("new-access-token", "new-refresh-token");
        given(authService.reissue(any(ReissueRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("유효하지 않은 RefreshToken이면 401을 반환한다")
    void reissue_실패_유효하지않은토큰() throws Exception {
        // given
        ReissueRequest request = new ReissueRequest("invalid-token");
        given(authService.reissue(any(ReissueRequest.class)))
                .willThrow(new InvalidRefreshTokenException("유효하지 않은 토큰입니다. 다시 로그인해주세요."));

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다. 다시 로그인해주세요."));
    }

    @Test
    @DisplayName("필수 값이 빠진 채로 요청하면 400을 반환한다")
    void reissue_실패_검증오류() throws Exception {
        // given
        String invalidRequestJson = """
                {
                    "refreshToken": ""
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이름/이메일이 일치하면 200과 아이디를 반환한다")
    void findUsername_성공() throws Exception {
        // given
        FindUsernameRequest request = new FindUsernameRequest("창", "chang@test.com");
        given(memberService.findUsername(any(FindUsernameRequest.class))).willReturn("chang123");

        // when & then
        mockMvc.perform(post("/api/auth/find-username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("chang123"));
    }

    @Test
    @DisplayName("일치하는 회원이 없으면 404를 반환한다")
    void findUsername_실패_일치하는회원없음() throws Exception {
        // given
        FindUsernameRequest request = new FindUsernameRequest("없음", "none@test.com");
        given(memberService.findUsername(any(FindUsernameRequest.class)))
                .willThrow(new UserNotFoundException("일치하는 회원 정보가 없습니다."));

        // when & then
        mockMvc.perform(post("/api/auth/find-username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("탈퇴한 회원이면 409를 반환한다")
    void findUsername_실패_탈퇴한회원() throws Exception {
        // given
        FindUsernameRequest request = new FindUsernameRequest("창", "chang@test.com");
        given(memberService.findUsername(any(FindUsernameRequest.class)))
                .willThrow(new WithdrawnMemberException("탈퇴한 회원입니다."));

        // when & then
        mockMvc.perform(post("/api/auth/find-username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("탈퇴한 회원입니다."));
    }

    @Test
    @DisplayName("재설정 요청을 하면 200을 반환한다")
    void requestPasswordReset_성공() throws Exception {
        // given
        PasswordResetRequest request = new PasswordResetRequest("chang@test.com");

        // when & then
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("입력하신 이메일로 재설정 링크를 보냈습니다. 메일함을 확인해주세요."));
    }

    @Test
    @DisplayName("이메일 형식이 잘못되면 400을 반환한다")
    void requestPasswordReset_실패_검증오류() throws Exception {
        // given
        String invalidRequestJson = """
                {
                    "email": "invalid-email"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효한 토큰이면 200을 반환한다")
    void confirmPasswordReset_성공() throws Exception {
        // given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("reset-token", "newpassword5678");

        // when & then
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 재설정되었습니다."));
    }

    @Test
    @DisplayName("유효하지 않거나 만료된 토큰이면 401을 반환한다")
    void confirmPasswordReset_실패_유효하지않은토큰() throws Exception {
        // given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("invalid-token", "newpassword5678");
        willThrow(new InvalidPasswordResetTokenException("유효하지 않은 요청입니다. 다시 시도해주세요."))
                .given(authService).confirmPasswordReset(any(PasswordResetConfirmRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 요청입니다. 다시 시도해주세요."));
    }

    @Test
    @DisplayName("로그인한 사용자가 로그아웃하면 200을 반환한다")
    void logout_성공() throws Exception {
        // given - addFilters=false라 @WithMockUser로는 request.getUserPrincipal()이 안 채워져서,
        // 요청 자체에 인증 정보를 직접 심어주는 방식을 쓴다
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("chang123", null, List.of());

        // when & then
        mockMvc.perform(post("/api/auth/logout").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));
    }
}
