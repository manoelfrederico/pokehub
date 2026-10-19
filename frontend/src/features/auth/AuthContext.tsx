import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useNavigate } from 'react-router-dom'
import * as authApi from '../../api/auth'
import { setUnauthorizedHandler } from '../../api/client'
import {
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from '../../lib/auth-storage'
import type { AuthRequest } from '../../types/auth'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const [isAuthenticated, setIsAuthenticated] = useState(
    () => getAccessToken() !== null,
  )

  const logout = useCallback(() => {
    clearAccessToken()
    setIsAuthenticated(false)
  }, [])

  const handleUnauthorized = useCallback(() => {
    clearAccessToken()
    setIsAuthenticated(false)
    navigate('/login', {
      replace: true,
      state: { message: 'Your session expired. Please sign in again.' },
    })
  }, [navigate])

  useEffect(() => {
    setUnauthorizedHandler(handleUnauthorized)
    return () => setUnauthorizedHandler(() => {})
  }, [handleUnauthorized])

  const login = useCallback(async (credentials: AuthRequest) => {
    const token = await authApi.login(credentials)
    setAccessToken(token.accessToken)
    setIsAuthenticated(true)
  }, [])

  const register = useCallback(async (credentials: AuthRequest) => {
    await authApi.register(credentials)
    const token = await authApi.login(credentials)
    setAccessToken(token.accessToken)
    setIsAuthenticated(true)
  }, [])

  const value = useMemo(
    () => ({ isAuthenticated, login, register, logout }),
    [isAuthenticated, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
