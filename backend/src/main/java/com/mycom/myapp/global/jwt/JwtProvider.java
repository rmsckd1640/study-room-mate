package com.mycom.myapp.global.jwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mycom.myapp.domain.member.entity.MemberRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(Long memberId, String username, MemberRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("memberId", memberId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getUsername(String token) {
        return getUsername(parseClaims(token));
    }

    public Long getMemberId(String token) {
        return parseClaims(token).get("memberId", Long.class);
    }

    public MemberRole getRole(String token) {
        return getRole(parseClaims(token));
    }

    public LocalDateTime getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 이미 파싱해둔 Claims에서 값만 뽑아 쓰는 용도 - 같은 토큰을 여러 번 파싱/서명검증하지 않기 위함 (JwtAuthFilter에서 사용)
    public String getUsername(Claims claims) {
        return claims.getSubject();
    }

    public MemberRole getRole(Claims claims) {
        return MemberRole.valueOf(claims.get("role", String.class));
    }

    // 서명 검증 + JSON 파싱을 한 번에 수행 - 호출부에서 결과(Claims)를 재사용해서 중복 파싱을 피할 수 있도록 public
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
