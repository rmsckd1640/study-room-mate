import { apiFetch } from './client'
import type { MemberResponse, MemberUpdateRequest, PasswordChangeRequest, WithdrawRequest } from './types'

export function getMyPage(id: number): Promise<MemberResponse> {
  return apiFetch<MemberResponse>(`/api/members/${id}`)
}

export function updateInfo(id: number, body: MemberUpdateRequest): Promise<MemberResponse> {
  return apiFetch<MemberResponse>(`/api/members/${id}`, { method: 'PATCH', body })
}

export function changePassword(id: number, body: PasswordChangeRequest): Promise<void> {
  return apiFetch<void>(`/api/members/${id}/password`, { method: 'PATCH', body })
}

export function withdraw(id: number, body: WithdrawRequest): Promise<void> {
  return apiFetch<void>(`/api/members/${id}`, { method: 'DELETE', body })
}

export function adminListMembers(): Promise<MemberResponse[]> {
  return apiFetch<MemberResponse[]>('/api/admin/members')
}

export function adminGetMember(id: number): Promise<MemberResponse> {
  return apiFetch<MemberResponse>(`/api/admin/members/${id}`)
}

export function adminWithdrawMember(id: number): Promise<void> {
  return apiFetch<void>(`/api/admin/members/${id}`, { method: 'DELETE' })
}
