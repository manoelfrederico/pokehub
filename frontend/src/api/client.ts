import { ApiError, NetworkError } from './ApiError'
import type { ProblemDetailBody } from '../types/problem'
import { getAccessToken } from '../lib/auth-storage'

const baseUrl = () =>
  (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ??
  '/api/v1'

let unauthorizedHandler: (() => void) | null = null

export function setUnauthorizedHandler(handler: () => void): void {
  unauthorizedHandler = handler
}

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown
}

export async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { body, headers: customHeaders, ...rest } = options
  const headers = new Headers(customHeaders)

  const token = getAccessToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  if (body !== undefined && !(body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  const url = `${baseUrl()}${path.startsWith('/') ? path : `/${path}`}`

  let response: Response
  try {
    response = await fetch(url, {
      ...rest,
      headers,
      body:
        body === undefined
          ? undefined
          : body instanceof FormData
            ? body
            : JSON.stringify(body),
    })
  } catch (cause) {
    throw new NetworkError(cause)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  const isJson =
    contentType.includes('application/json') ||
    contentType.includes('application/problem+json')

  if (!response.ok) {
    if (response.status === 401 && unauthorizedHandler) {
      unauthorizedHandler()
    }
    if (isJson) {
      const problem = (await response.json()) as ProblemDetailBody
      if (!problem.status) {
        problem.status = response.status
      }
      throw new ApiError(problem)
    }
    throw new ApiError({
      status: response.status,
      title: response.statusText || 'Request failed',
      detail: `HTTP ${response.status}`,
    })
  }

  if (!isJson) {
    return undefined as T
  }

  return (await response.json()) as T
}
