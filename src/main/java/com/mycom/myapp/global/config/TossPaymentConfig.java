package com.mycom.myapp.global.config;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TossPaymentConfig {

	@Value("${toss.payment.toss-secret-key}")
	private String tossSecretKey;
	
	@Value("${toss.payment.base-url}")
	private String baseUrl;
	
	@Bean
	public WebClient tossPaymentWebClient() {
		String encodedAuth = Base64.getEncoder()
				.encodeToString((tossSecretKey + ":").getBytes());
		
		return WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader("Authorization", "Basic " + encodedAuth)
				.defaultHeader("Content-Type", "application/json")
				.build();
	}
	
}
