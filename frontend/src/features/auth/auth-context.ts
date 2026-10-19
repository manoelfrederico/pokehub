import { createContext } from 'react'
import type { AuthRequest } from '../../types/auth'

export type AuthContextValue = {
  isAuthenticated: boolean
  login: (credentials: AuthRequest) => Promise<void>
  register: (credentials: AuthRequest) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
