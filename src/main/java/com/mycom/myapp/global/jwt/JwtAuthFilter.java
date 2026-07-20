package com.mycom.myapp.global.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mycom.myapp.domain.member.entity.MemberRole;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// 요청마다 한 번씩 실행되며, Access Token을 검사해서 SecurityContext에 인증 정보를 채워주는 필터.
// 토큰이 없거나 유효하지 않아도 여기서 요청을 막지는 않는다 — 접근 허용 여부는 SecurityConfig가 결정한다.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // 클라이언트가 토큰을 실어 보내는 헤더 이름
    private static final String HEADER_NAME = "Authorization";
    // 헤더 값 앞에 붙는 접두사 (예: "Bearer eyJhbGci...")
    private static final String TOKEN_PREFIX = "Bearer ";

    // 토큰 발급/파싱/검증을 담당하는 컴포넌트 (생성자 주입)
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 순수 토큰 문자열만 추출
        String token = resolveToken(request);

        if (token != null && jwtProvider.isValid(token)) {
            // 토큰 안에 담겨있던 정보를 꺼내서(DB 조회 없음), 인증 객체를 구성
            String username = jwtProvider.getUsername(token);
            MemberRole role = jwtProvider.getRole(token);

            // Spring Security는 권한 문자열이 "ROLE_" 접두사로 시작해야 hasRole()과 매칭된다
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 이후 @PreAuthorize, requestMatchers 등이 이 값을 보고 인가 여부를 판단한다
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 인증 성공/실패 여부와 상관없이 다음 필터로 넘긴다(권한 판단은 다른 곳에 맡김)
        filterChain.doFilter(request, response);
    }

    // "Authorization: Bearer <token>" 형식에서 <token> 부분만 잘라낸다
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
