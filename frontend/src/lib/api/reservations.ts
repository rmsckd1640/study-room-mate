import { apiFetch } from './client'
import type { ReservationInsertRequest, ReservationResponse, ReservationStatus } from './types'

export function insertReservation(roomId: number, body: ReservationInsertRequest): Promise<ReservationResponse> {
  return apiFetch<ReservationResponse>(`/api/reservation/${roomId}`, { method: 'POST', body })
}

export function cancelReservation(id: number, reason: string): Promise<ReservationResponse> {
  return apiFetch<ReservationResponse>(`/api/reservation/${id}/cancel`, { method: 'POST', query: { arg1: reason } })
}

export function getMyReservations(): Promise<ReservationResponse[]> {
  return apiFetch<ReservationResponse[]>('/api/reservation/my')
}

export function getMyReservationById(id: number): Promise<ReservationResponse[]> {
  return apiFetch<ReservationResponse[]>(`/api/reservation/my/${id}`)
}

export function getReservationsByStatus(status: ReservationStatus): Promise<ReservationResponse[]> {
  return apiFetch<ReservationResponse[]>('/api/reservation/status', { query: { status } })
}

export function adminListReservations(): Promise<ReservationResponse[]> {
  return apiFetch<ReservationResponse[]>('/api/admin/reservation')
}

export function adminConfirmReservation(id: number, status: ReservationStatus): Promise<ReservationResponse> {
  return apiFetch<ReservationResponse>(`/api/admin/reservation/${id}/confirm`, { method: 'POST', query: { arg1: status } })
}

export function adminRejectReservation(id: number, reason?: string): Promise<ReservationResponse> {
  return apiFetch<ReservationResponse>(`/api/admin/reservation/${id}/reject`, { method: 'POST', query: { arg1: reason } })
}
