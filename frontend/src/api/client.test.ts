import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { clearAccessToken, setAccessToken } from '../lib/auth-storage'
import { ApiError, NetworkError } from './ApiError'
import { request, setUnauthorizedHandler } from './client'

describe('request', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
    clearAccessToken()
    setUnauthorizedHandler(() => {})
  })

  afterEach(() => {
    vi.mocked(fetch).mockReset()
  })

  it('returns parsed JSON on success', async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    const data = await request<{ ok: boolean }>('/pokemon')
    expect(data.ok).toBe(true)
    expect(fetch).toHaveBeenCalledWith('/api/v1/pokemon', expect.any(Object))
  })

  it('maps problem details to ApiError', async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(
        JSON.stringify({
          title: 'Bad request',
          status: 400,
          detail: 'Invalid',
          errors: [{ field: 'limit', message: 'too large' }],
        }),
        {
          status: 400,
          headers: { 'Content-Type': 'application/problem+json' },
        },
      ),
    )

    await expect(request('/pokemon')).rejects.toMatchObject({
      status: 400,
      title: 'Bad request',
      detail: 'Invalid',
      errors: [{ field: 'limit', message: 'too large' }],
    } satisfies Partial<ApiError>)
  })

  it('handles 204 with empty body', async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(null, { status: 204 }))

    const result = await request<void>('/pokemon/1/sync', { method: 'DELETE' })
    expect(result).toBeUndefined()
  })

  it('throws NetworkError when fetch fails', async () => {
    vi.mocked(fetch).mockRejectedValue(new TypeError('Failed to fetch'))

    await expect(request('/pokemon')).rejects.toBeInstanceOf(NetworkError)
  })

  it('sends Authorization header when token is stored', async () => {
    setAccessToken('secret-token')
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({}), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    await request('/pokemon/1/sync', { method: 'POST' })

    const [, init] = vi.mocked(fetch).mock.calls[0]
    const headers = init?.headers as Headers
    expect(headers.get('Authorization')).toBe('Bearer secret-token')
  })

  it('invokes unauthorized handler on 401', async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    vi.mocked(fetch).mockResolvedValue(
      new Response(
        JSON.stringify({ title: 'Unauthorized', status: 401 }),
        {
          status: 401,
          headers: { 'Content-Type': 'application/problem+json' },
        },
      ),
    )

    await expect(request('/pokemon/1/sync', { method: 'POST' })).rejects.toBeInstanceOf(
      ApiError,
    )
    expect(handler).toHaveBeenCalledOnce()
  })
})
