import { apiFetch } from './client'
import type { PageResponse, RoomCreateRequest, RoomResponseDto, RoomUpdateRequest } from './types'

export function listRooms(page = 0, size = 100): Promise<PageResponse<RoomResponseDto>> {
  return apiFetch<PageResponse<RoomResponseDto>>('/api/rooms', { query: { page, size } })
}

export function getRoom(id: number): Promise<RoomResponseDto> {
  return apiFetch<RoomResponseDto>(`/api/rooms/${id}`)
}

export function createRoom(body: RoomCreateRequest): Promise<RoomResponseDto> {
  return apiFetch<RoomResponseDto>('/api/rooms', { method: 'POST', body })
}

export function updateRoom(id: number, body: RoomUpdateRequest): Promise<RoomResponseDto> {
  return apiFetch<RoomResponseDto>(`/api/rooms/${id}`, { method: 'PATCH', body })
}

export function deleteRoom(id: number): Promise<void> {
  return apiFetch<void>(`/api/rooms/${id}`, { method: 'DELETE' })
}

export function searchRoomsByName(name: string): Promise<RoomResponseDto[]> {
  return apiFetch<RoomResponseDto[]>('/api/rooms/search/name', { query: { name } })
}

export function searchRoomsByPrice(price: number): Promise<RoomResponseDto[]> {
  return apiFetch<RoomResponseDto[]>('/api/rooms/search/price', { query: { price } })
}

export function searchRoomsByCapacity(capacity: number): Promise<RoomResponseDto[]> {
  return apiFetch<RoomResponseDto[]>('/api/rooms/search/capacity', { query: { capacity } })
}
