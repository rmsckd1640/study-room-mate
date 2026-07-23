package com.mycom.myapp.global.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse.Failure;
import com.mycom.myapp.global.exception.TossPaymentException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TossErrorInterceptor implements ClientHttpRequestInterceptor {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		ClientHttpResponse response;
		try {
			response = execution.execute(request, body);
		} catch (IOException e) {
			log.error("Toss 요청 실패(타임아웃/연결 오류) - {} {}, requestBody: {}",
					request.getMethod(), request.getURI(), new String(body, StandardCharsets.UTF_8), e);
			throw new TossPaymentException("결제 서버와 통신할 수 없습니다. 잠시 후 다시 시도해주세요.",
					HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT);
		}

		if (response.getStatusCode().is2xxSuccessful()) {
			return response;
		}

		String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		HttpStatusCode tossStatus = response.getStatusCode();
		HttpStatus clientStatus = tossStatus.is4xxClientError() ? HttpStatus.BAD_REQUEST : HttpStatus.BAD_GATEWAY;

		log.error("Toss 응답 실패 - {} {}, requestBody: {}, status: {}, responseBody: {}",
				request.getMethod(), request.getURI(), new String(body, StandardCharsets.UTF_8), tossStatus, responseBody);
		throw new TossPaymentException(parseMessage(responseBody), tossStatus, clientStatus);
	}

	private String parseMessage(String body) {
		try {
			return objectMapper.readValue(body, Failure.class).message();
		} catch (Exception e) {
			return "결제 처리 중 오류가 발생했습니다.";
		}
	}

}
