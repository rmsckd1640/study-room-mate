package com.mycom.myapp.global.config;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TossPaymentConfig {

	@Value("${toss.payment.toss-secret-key}")
	private String tossSecretKey;

	@Value("${toss.payment.base-url}")
	private String baseUrl;

	@Bean
	public RestClient tossPaymentRestClient() {
		String encodedAuth = Base64.getEncoder()
				.encodeToString((tossSecretKey + ":").getBytes());

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(6000);
		requestFactory.setReadTimeout(5000);

		return RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.requestInterceptor(new TossErrorInterceptor())
				.defaultHeader("Authorization", "Basic " + encodedAuth)
				.defaultHeader("Content-Type", "application/json")
				.build();
	}

}
