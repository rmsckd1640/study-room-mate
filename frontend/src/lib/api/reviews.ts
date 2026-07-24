import { apiFetch } from './client'
import type { PageResponse, ReviewCreateRequest, ReviewResponseDto, ReviewUpdateRequest, RoomRatingSummaryDto } from './types'

export function createReview(body: ReviewCreateRequest): Promise<ReviewResponseDto> {
  return apiFetch<ReviewResponseDto>('/api/reviews', { method: 'POST', body })
}

export function updateReview(id: number, body: ReviewUpdateRequest): Promise<ReviewResponseDto> {
  return apiFetch<ReviewResponseDto>(`/api/reviews/${id}`, { method: 'PATCH', body })
}

export function deleteReview(id: number): Promise<void> {
  return apiFetch<void>(`/api/reviews/${id}`, { method: 'DELETE' })
}

export function getReviewsByRoom(roomId: number, page = 0, size = 20): Promise<PageResponse<ReviewResponseDto>> {
  return apiFetch<PageResponse<ReviewResponseDto>>(`/api/reviews/room/${roomId}`, { query: { page, size } })
}

export function getRatingSummary(roomId: number): Promise<RoomRatingSummaryDto> {
  return apiFetch<RoomRatingSummaryDto>(`/api/reviews/room/${roomId}/rating-summary`)
}

export function getReviewsByMember(memberId: number): Promise<ReviewResponseDto[]> {
  return apiFetch<ReviewResponseDto[]>(`/api/reviews/member/${memberId}`)
}
