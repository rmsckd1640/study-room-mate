package com.mycom.myapp.domain.member;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.member.controller.MemberController;
import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.MemberUpdateRequest;
import com.mycom.myapp.domain.member.dto.PasswordChangeRequest;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.dto.WithdrawRequest;
import com.mycom.myapp.domain.member.entity.MemberGrade;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.service.MemberService;
import com.mycom.myapp.global.exception.DuplicateUsernameException;
import com.mycom.myapp.global.exception.InvalidCredentialsException;
import com.mycom.myapp.global.jwt.JwtAccessDeniedHandler;
import com.mycom.myapp.global.jwt.JwtAuthFilter;
import com.mycom.myapp.global.jwt.JwtAuthenticationEntryPoint;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("정상적인 요청으로 회원가입하면 201을 반환한다")
    void signup_성공() throws Exception {
        // given
        SignupRequest request = new SignupRequest("chang123", "password1234", "chang@test.com", "창");
        MemberResponse response = new MemberResponse(1L, "chang123", "chang@test.com", "창", MemberRole.USER, MemberGrade.BRONZE);
        given(memberService.signup(any(SignupRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("chang123"))
                .andExpect(jsonPath("$.data.email").value("chang@test.com"));
    }

    @Test
    @DisplayName("필수 값이 빠진 채로 요청하면 400을 반환한다")
    void signup_실패_검증오류() throws Exception {
        // given - username이 빈 문자열인 잘못된 요청
        String invalidRequestJson = """
                {
                    "username": "",
                    "password": "password1234",
                    "email": "chang@test.com",
                    "name": "창"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 사용 중인 아이디면 409를 반환한다")
    void signup_실패_아이디중복() throws Exception {
        // given
        SignupRequest request = new SignupRequest("chang123", "password1234", "chang@test.com", "창");
        given(memberService.signup(any(SignupRequest.class)))
                .willThrow(new DuplicateUsernameException("이미 사용 중인 아이디입니다."));

        // when & then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
    }

    // addFilters=false라 필터를 거쳐 SecurityContextHolder를 채워주는 방식(@WithMockUser, .with(authentication()))이
    // 전부 안 먹혀서, 테스트 스레드에서 SecurityContextHolder에 직접 넣어준다
    private void authenticateAs(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("본인이 조회하면 200과 마이페이지 정보를 반환한다")
    void getMyPage_성공() throws Exception {
        // given
        MemberResponse response = new MemberResponse(1L, "chang123", "chang@test.com", "창", MemberRole.USER, MemberGrade.BRONZE);
        given(memberService.getMyPage(1L, "chang123")).willReturn(response);
        authenticateAs("chang123");

        // when & then
        mockMvc.perform(get("/api/members/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("chang123"));
    }

    @Test
    @DisplayName("본인이 아니면 403을 반환한다")
    void getMyPage_실패_본인아님() throws Exception {
        // given
        given(memberService.getMyPage(1L, "other_user"))
                .willThrow(new AccessDeniedException("본인만 접근할 수 있습니다."));
        authenticateAs("other_user");

        // when & then
        mockMvc.perform(get("/api/members/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("정상적인 정보 수정 요청이면 200을 반환한다")
    void updateInfo_성공() throws Exception {
        // given
        MemberUpdateRequest request = new MemberUpdateRequest("새이름", "new@test.com");
        MemberResponse response = new MemberResponse(1L, "chang123", "new@test.com", "새이름", MemberRole.USER, MemberGrade.BRONZE);
        given(memberService.updateInfo(any(Long.class), anyString(), any(MemberUpdateRequest.class))).willReturn(response);
        authenticateAs("chang123");

        // when & then
        mockMvc.perform(patch("/api/members/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("새이름"));
    }

    @Test
    @DisplayName("이메일 형식이 잘못되면 400을 반환한다")
    void updateInfo_실패_검증오류() throws Exception {
        // given
        String invalidRequestJson = """
                {
                    "name": "새이름",
                    "email": "invalid-email"
                }
                """;
        authenticateAs("chang123");

        // when & then
        mockMvc.perform(patch("/api/members/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("정상적인 비밀번호 변경 요청이면 200을 반환한다")
    void changePassword_성공() throws Exception {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("password1234", "newpassword5678");
        authenticateAs("chang123");

        // when & then
        mockMvc.perform(patch("/api/members/{id}/password", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다."));
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 401을 반환한다")
    void changePassword_실패_비밀번호불일치() throws Exception {
        // given
        PasswordChangeRequest request = new PasswordChangeRequest("wrong-password", "newpassword5678");
        willThrow(new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다."))
                .given(memberService).changePassword(anyLong(), anyString(), any(PasswordChangeRequest.class));
        authenticateAs("chang123");

        // when & then
        mockMvc.perform(patch("/api/members/{id}/password", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("정상적인 탈퇴 요청이면 200을 반환한다")
    void withdraw_성공() throws Exception {
        // given
        WithdrawRequest request = new WithdrawRequest("password1234");
        authenticateAs("chang123");

        // when & then
        mockMvc.perform(delete("/api/members/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("탈퇴가 완료되었습니다."));
    }
}
