package com.mycom.myapp.domain.review;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.domain.review.controller.ReviewController;
import com.mycom.myapp.domain.review.dto.ReviewCreateRequest;
import com.mycom.myapp.domain.review.dto.ReviewResponseDto;
import com.mycom.myapp.domain.review.dto.ReviewUpdateRequest;
import com.mycom.myapp.domain.review.dto.RoomRatingSummaryDto;
import com.mycom.myapp.global.exception.DuplicateReviewException;
import com.mycom.myapp.global.exception.ReviewAccessDeniedException;
import com.mycom.myapp.global.exception.ReviewNotAllowedException;
import com.mycom.myapp.global.jwt.JwtProvider;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false) // JwtAuthFilter를 비활성화하고, 대신 아래에서 인증 정보를 직접 주입
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper(); // @Autowired 대신 직접 생성

	@MockitoBean
	private com.mycom.myapp.domain.review.service.ReviewService reviewService;

	@MockitoBean
	private JwtProvider jwtProvider;

	private RequestPostProcessor withAuth(String username) {
		return request -> {
			Authentication auth = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
			SecurityContextHolder.getContext().setAuthentication(auth);
			return request;
		};
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("본인이 작성한 리뷰를 수정하면 200을 반환한다")
	void update_성공() throws Exception {
		// given
		ReviewUpdateRequest request = new ReviewUpdateRequest(4, "다시 보니 좋아요");
		ReviewResponseDto response = new ReviewResponseDto(1L, 10L, 5L, 4, "다시 보니 좋아요", null);
		//		given(reviewService.updateReview(eq("user1"), eq(1L), any(ReviewUpdateRequest.class))).willReturn(response);
		given(reviewService.updateReview(any(), any(), any())).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/reviews/{id}", 1L).with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.data.rating").value(4));
	}

	@Test
	@DisplayName("리뷰를 생성하면 201과 데이터를 반환한다")
	void create_성공() throws Exception {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(5L, 5, "최고예요");
		ReviewResponseDto response = new ReviewResponseDto(1L, 10L, 5L, 5, "최고예요", null);
		given(reviewService.createReview(eq("user1"), any(ReviewCreateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/reviews").with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.message").value("리뷰 생성 성공")).andExpect(jsonPath("$.data.rating").value(5));
	}

	@Test
	@DisplayName("평점이 범위를 벗어나면 400을 반환한다")
	void create_실패_검증오류() throws Exception {
		// given - rating이 6점(범위 초과)인 잘못된 요청
		String invalidJson = """
				{ "roomId": 5, "rating": 6, "content": "최고예요" }
				""";

		// when & then
		mockMvc.perform(post("/api/reviews").with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(invalidJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("확정된 예약이 없으면 403을 반환한다")
	void create_실패_예약없음() throws Exception {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(5L, 5, "최고예요");
		given(reviewService.createReview(eq("user1"), any(ReviewCreateRequest.class))).willThrow(new ReviewNotAllowedException("확정된 예약이 있어야 리뷰를 작성할 수 있습니다."));

		// when & then
		mockMvc.perform(post("/api/reviews").with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("확정된 예약이 있어야 리뷰를 작성할 수 있습니다."));
	}

	@Test
	@DisplayName("이미 리뷰를 작성했으면 409를 반환한다")
	void create_실패_중복리뷰() throws Exception {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(5L, 5, "최고예요");
		given(reviewService.createReview(eq("user1"), any(ReviewCreateRequest.class))).willThrow(new DuplicateReviewException("이미 이 방에 대한 리뷰를 작성했습니다."));

		// when & then
		mockMvc.perform(post("/api/reviews").with(withAuth("user1")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("이미 이 방에 대한 리뷰를 작성했습니다."));
	}

	@Test
	@DisplayName("본인이 작성하지 않은 리뷰를 수정하면 403을 반환한다")
	void update_실패_권한없음() throws Exception {
		// given
		ReviewUpdateRequest request = new ReviewUpdateRequest(4, "다시 보니 좋아요");
		given(reviewService.updateReview(eq("user2"), eq(1L), any(ReviewUpdateRequest.class))).willThrow(new ReviewAccessDeniedException("본인이 작성한 리뷰만 수정/삭제할 수 있습니다."));

		// when & then
		mockMvc.perform(patch("/api/reviews/{id}", 1L).with(withAuth("user2")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("본인이 작성한 리뷰만 수정/삭제할 수 있습니다."));
	}

	@Test
	@DisplayName("본인이 작성한 리뷰를 삭제하면 200을 반환한다")
	void delete_성공() throws Exception {
		// when & then
		mockMvc.perform(delete("/api/reviews/{id}", 1L).with(withAuth("user1"))).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("리뷰 삭제 성공"));
	}

	@Test
	@DisplayName("특정 회원이 작성한 리뷰 목록을 조회한다")
	void getByMember_성공() throws Exception {
		// given
		ReviewResponseDto response = new ReviewResponseDto(1L, 10L, 5L, 5, "최고예요", null);
		given(reviewService.getReviewsByMember(10L)).willReturn(List.of(response));

		// when & then
		mockMvc.perform(get("/api/reviews/member/{memberId}", 10L)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].rating").value(5));
	}

	@Test
	@DisplayName("특정 room의 리뷰를 평점순으로 페이징 조회한다")
	void getByRoom_성공() throws Exception {
		// given
		ReviewResponseDto response = new ReviewResponseDto(1L, 10L, 5L, 5, "최고예요", null);
		Page<ReviewResponseDto> page = new PageImpl<>(List.of(response));
		given(reviewService.getReviewsByRoom(eq(5L), any())).willReturn(page);

		// when & then
		mockMvc.perform(get("/api/reviews/room/{roomId}", 5L)).andExpect(status().isOk()).andExpect(jsonPath("$.data.content[0].rating").value(5));
	}

	@Test
	@DisplayName("room의 평균 평점 요약을 조회한다")
	void getRatingSummary_성공() throws Exception {
		// given
		RoomRatingSummaryDto summary = new RoomRatingSummaryDto(4.5, 10L);
		given(reviewService.getRatingSummary(5L)).willReturn(summary);

		// when & then
		mockMvc.perform(get("/api/reviews/room/{roomId}/rating-summary", 5L)).andExpect(status().isOk()).andExpect(jsonPath("$.data.averageRating").value(4.5)).andExpect(jsonPath("$.data.reviewCount").value(10));
	}
}
