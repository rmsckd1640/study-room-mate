package com.mycom.myapp.domain.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.payment.dto.TossConfirmRequest;
import com.mycom.myapp.domain.payment.dto.TossPaymentResponse;
import com.mycom.myapp.domain.payment.service.TossPaymentService;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private TossPaymentService tossPaymentService;

	private TossPaymentResponse doneResponse(String orderId, String paymentKey, long amount) {
		return new TossPaymentResponse(
				null, paymentKey, null, orderId, null, null, null, null,
				amount, amount, "DONE", null, null, null, null, null, null, null
		);
	}

	@Test
	public void confirm_결제확정_성공() throws Exception {
		TossConfirmRequest request = new TossConfirmRequest("pk_test_123", "order-1", 5000);

		when(tossPaymentService.confirm(any()))
				.thenReturn(doneResponse("order-1", "pk_test_123", 5000));

		mockMvc.perform(post("/api/payment/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DONE"))
				.andExpect(jsonPath("$.orderId").value("order-1"))
				.andExpect(jsonPath("$.paymentKey").value("pk_test_123"));
	}

	@Test
	public void confirm_모든값이_없으면_400을_반환한다() throws Exception {
		mockMvc.perform(post("/api/payment/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void confirm_paymentKey가_없으면_400을_반환한다() throws Exception {
		mockMvc.perform(post("/api/payment/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"orderId\":\"order-1\",\"amount\":5000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void confirm_서비스에서_예외발생시_500을_반환한다() throws Exception {
		TossConfirmRequest request = new TossConfirmRequest("pk_test_123", "order-1", 5000);

		when(tossPaymentService.confirm(any()))
				.thenThrow(new IllegalStateException("결제 금액 != 예약 금액"));

		mockMvc.perform(post("/api/payment/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isInternalServerError());
	}

}
