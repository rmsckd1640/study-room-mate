package com.mycom.myapp.domain.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mycom.myapp.domain.auth.controller.AuthController;
import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.reservation.controller.ReservationController;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerTest {

	@MockitoBean
	ReservationController reservationController;
	
	@MockitoBean
	AuthController authController;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	@Order(1)
	public void testList() {
		LoginRequest request = new LoginRequest("uho", "1234");
		
		
	}
	
}
