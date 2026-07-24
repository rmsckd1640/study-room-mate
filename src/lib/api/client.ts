import type { LoginResponse, ResultDto } from './types'

const ACCESS_TOKEN_KEY = 'srm_access_token'
const REFRESH_TOKEN_KEY = 'srm_refresh_token'

export const tokenStore = {
  getAccessToken: (): string | null => localStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: (): string | null => localStorage.getItem(REFRESH_TOKEN_KEY),
  setTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}

export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export class AuthExpiredError extends Error {
  constructor() {
    super('로그인이 만료되었습니다. 다시 로그인해주세요.')
    this.name = 'AuthExpiredError'
  }
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

type QueryValue = string | number | boolean | undefined | null

interface ApiFetchOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
  query?: Record<string, QueryValue>
  skipAuth?: boolean
  skipRetry?: boolean
}

function buildUrl(path: string, query?: Record<string, QueryValue>): string {
  const url = new URL(BASE_URL + path, window.location.origin)
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null) url.searchParams.set(key, String(value))
    }
  }
  return url.pathname + url.search
}

async function rawRequest(path: string, options: ApiFetchOptions): Promise<{ status: number; body: unknown }> {
  const { body, query, skipAuth, headers, ...rest } = options
  const finalHeaders: Record<string, string> = { Accept: 'application/json', ...(headers as Record<string, string> | undefined) }
  if (body !== undefined) finalHeaders['Content-Type'] = 'application/json'
  if (!skipAuth) {
    const token = tokenStore.getAccessToken()
    if (token) finalHeaders.Authorization = `Bearer ${token}`
  }

  const res = await fetch(buildUrl(path, query), {
    ...rest,
    headers: finalHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  const text = await res.text()
  let parsed: unknown = null
  if (text) {
    try {
      parsed = JSON.parse(text)
    } catch {
      parsed = text
    }
  }
  return { status: res.status, body: parsed }
}

let reissuePromise: Promise<boolean> | null = null

async function tryReissue(): Promise<boolean> {
  const refreshToken = tokenStore.getRefreshToken()
  if (!refreshToken) return false

  if (!reissuePromise) {
    reissuePromise = (async () => {
      try {
        const { status, body } = await rawRequest('/api/auth/reissue', {
          method: 'POST',
          body: { refreshToken },
          skipAuth: true,
        })
        const result = body as ResultDto<LoginResponse> | null
        if (status >= 200 && status < 300 && result?.data?.accessToken) {
          tokenStore.setTokens(result.data.accessToken, result.data.refreshToken)
          return true
        }
        return false
      } catch {
        return false
      } finally {
        reissuePromise = null
      }
    })()
  }
  return reissuePromise
}

/** ResultDto<T> 봉투를 벗겨 data만 반환. 봉투가 없는 응답(TossPaymentResponse 등)은 그대로 반환. */
export async function apiFetch<T>(path: string, options: ApiFetchOptions = {}): Promise<T> {
  const { status, body } = await rawRequest(path, options)

  if (status >= 200 && status < 300) {
    if (body && typeof body === 'object' && 'data' in (body as Record<string, unknown>)) {
      return (body as Record<string, unknown>).data as T
    }
    return body as T
  }

  if (status === 401 && !options.skipAuth && !options.skipRetry) {
    const reissued = await tryReissue()
    if (reissued) {
      return apiFetch<T>(path, { ...options, skipRetry: true })
    }
    tokenStore.clear()
    throw new AuthExpiredError()
  }

  const message =
    (body && typeof body === 'object' && 'message' in (body as Record<string, unknown>)
      ? String((body as Record<string, unknown>).message)
      : null) || `요청에 실패했습니다. (${status})`
  throw new ApiError(status, message)
}

/** JWT accessToken payload에서 회원 id로 추정되는 클레임을 순서대로 탐색. */
export function decodeJwtMemberId(token: string): number | null {
  try {
    const payloadPart = token.split('.')[1]
    if (!payloadPart) return null
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    )
    const payload = JSON.parse(json) as Record<string, unknown>
    for (const key of ['id', 'memberId', 'sub']) {
      const value = payload[key]
      if (value === undefined || value === null) continue
      const num = Number(value)
      if (!Number.isNaN(num)) return num
    }
    return null
  } catch {
    return null
  }
}
