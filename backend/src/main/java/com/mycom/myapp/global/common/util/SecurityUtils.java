package com.mycom.myapp.global.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class SecurityUtils {
	
	public String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
			throw new UsernameNotFoundException("로그인한 사용자를 찾을 수 없습니다.");
		}
		
		return (String) authentication.getPrincipal();
	}
	
	public boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		return authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
	
}
