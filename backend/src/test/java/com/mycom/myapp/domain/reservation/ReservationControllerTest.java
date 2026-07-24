package com.mycom.myapp.domain.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;
import com.mycom.myapp.domain.reservation.dto.ReservationResponse;
import com.mycom.myapp.domain.reservation.service.ReservationService;
import com.mycom.myapp.global.common.dto.ResultDto;
import com.mycom.myapp.global.common.enums.ReservationStatus;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ReservationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private ReservationService reservationService;

	private ReservationResponse sampleResponse() {
		return ReservationResponse.builder()
				.id(1L)
				.roomId(10L)
				.orderId("order-1")
				.reservationDate(LocalDate.of(2026, 12, 1))
				.startTime(LocalDateTime.of(2026, 12, 1, 10, 0))
				.endTime(LocalDateTime.of(2026, 12, 1, 11, 0))
				.status(ReservationStatus.PENDING)
				.build();
	}

	@Test
	public void insert_예약생성_성공() throws Exception {
		when(reservationService.insert(eq(10L), any()))
				.thenReturn(ResultDto.<ReservationResponse>builder().data(sampleResponse()).build());

		ReservationInsertRequest request = new ReservationInsertRequest(
				LocalDate.of(2026, 12, 1),
				LocalDateTime.of(2026, 12, 1, 10, 0),
				LocalDateTime.of(2026, 12, 1, 11, 0),
				5000L
		);

		mockMvc.perform(post("/api/reservation/10")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("PENDING"))
				.andExpect(jsonPath("$.data.orderId").value("order-1"));
	}

	@Test
	public void insert_필수값이_없으면_400을_반환한다() throws Exception {
		mockMvc.perform(post("/api/reservation/10")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void cancel_예약취소_성공() throws Exception {
		when(reservationService.cancel(eq(1L), anyString()))
				.thenReturn(ResultDto.<ReservationResponse>builder().message("cancled successfuly").build());

		mockMvc.perform(post("/api/reservation/1/cancel").param("reason", "단순 변심"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("cancled successfuly"));
	}

	@Test
	public void myList_내예약_조회_성공() throws Exception {
		when(reservationService.myList())
				.thenReturn(ResultDto.<List<ReservationResponse>>builder().data(List.of(sampleResponse())).build());

		mockMvc.perform(get("/api/reservation/my"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].orderId").value("order-1"));
	}

	@Test
	public void statusList_CANCELLED로_조회하면_400을_반환한다() throws Exception {
		mockMvc.perform(get("/api/reservation/status").param("status", "CANCELLED"))
				.andExpect(status().isBadRequest());

		verify(reservationService, never()).statusList(any(ReservationStatus.class));
	}

	@Test
	public void statusList_PENDING으로_조회하면_200을_반환한다() throws Exception {
		when(reservationService.statusList(ReservationStatus.PENDING))
				.thenReturn(ResultDto.<List<ReservationResponse>>builder().data(List.of(sampleResponse())).build());

		mockMvc.perform(get("/api/reservation/status").param("status", "PENDING"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].status").value("PENDING"));
	}

}
