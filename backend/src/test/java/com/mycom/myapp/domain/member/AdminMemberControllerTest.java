package com.mycom.myapp.domain.member;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.member.controller.AdminMemberController;
import com.mycom.myapp.domain.member.dto.MemberResponse;
import com.mycom.myapp.domain.member.entity.MemberGrade;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.member.service.MemberService;
import com.mycom.myapp.global.jwt.JwtAccessDeniedHandler;
import com.mycom.myapp.global.jwt.JwtAuthFilter;
import com.mycom.myapp.global.jwt.JwtAuthenticationEntryPoint;

@WebMvcTest(AdminMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    // addFilters=false라 필터를 거쳐 SecurityContextHolder를 채워주는 방식이 안 먹혀서 직접 채워준다
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("관리자가 요청하면 전체 회원 목록을 반환한다")
    void getAllMembers_성공() throws Exception {
        // given
        MemberResponse response = new MemberResponse(1L, "chang123", "chang@test.com", "창", MemberRole.USER, MemberGrade.BRONZE);
        given(memberService.getAllMembers()).willReturn(List.of(response));
        authenticateAsAdmin();

        // when & then
        mockMvc.perform(get("/api/admin/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("chang123"));
    }

    @Test
    @DisplayName("관리자가 요청하면 회원 단건을 반환한다")
    void getMember_성공() throws Exception {
        // given
        MemberResponse response = new MemberResponse(1L, "chang123", "chang@test.com", "창", MemberRole.USER, MemberGrade.BRONZE);
        given(memberService.getMember(1L)).willReturn(response);
        authenticateAsAdmin();

        // when & then
        mockMvc.perform(get("/api/admin/members/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("chang123"));
    }

    @Test
    @DisplayName("관리자가 요청하면 회원을 강제 탈퇴시킨다")
    void withdrawMember_성공() throws Exception {
        // given
        authenticateAsAdmin();

        // when & then
        mockMvc.perform(delete("/api/admin/members/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("탈퇴 처리되었습니다."));
    }
}
