package com.mycom.myapp.global.common.util;

import java.util.Random;

import org.springframework.stereotype.Component;

import com.mycom.myapp.domain.member.entity.Member;
import com.mycom.myapp.domain.member.entity.MemberRole;
import com.mycom.myapp.domain.room.entity.Room;

@Component
public class RandomUtilImpl implements RandomUtils {
	
	public Member randomMember() {
		return Member.builder()
				.email(randomEmail())
				.password(randomString(8))
				.role(MemberRole.USER)
				.name(randomString(5))
				.username(randomString(7))
				.build();
	}
	
	public Room randomRoom() {
		return Room.builder()
				.capacity(randomInt(10))
				.location(randomString(5))
				.name(randomString(3))
				.price(randomInt(3000))
				.build();
	}
	
	private String randomEmail() {
		return randomString(6) + "@test.com";
	}
	
	private int randomInt(int length) {
		Random random = new Random();
		
		return random.nextInt(length);
	}
	
	private String randomString(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(str.length());
			sb.append(str.charAt(index));
		}
		
		return sb.toString();
	}
	
}
