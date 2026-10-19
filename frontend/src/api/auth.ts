import { request } from './client'
import type { AuthRequest, TokenResponse, UserResponse } from '../types/auth'

export function register(body: AuthRequest): Promise<UserResponse> {
  return request<UserResponse>('/auth/register', { method: 'POST', body })
}

export function login(body: AuthRequest): Promise<TokenResponse> {
  return request<TokenResponse>('/auth/login', { method: 'POST', body })
}
