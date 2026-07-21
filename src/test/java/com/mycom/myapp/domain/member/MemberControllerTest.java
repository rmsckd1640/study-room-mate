package com.mycom.myapp.domain.member;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.member.controller.MemberController;
import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.dto.SignupRequest;
import com.mycom.myapp.domain.member.entity.MemberGrade;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.service.MemberService;
import com.mycom.myapp.global.exception.DuplicateUsernameException;
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
}
