import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { Route, Routes } from 'react-router-dom'
import { LoginPage } from '../features/auth/LoginPage'
import { setAccessToken } from '../lib/auth-storage'
import { renderWithProviders } from '../test/test-utils'
import { ProtectedRoute } from './ProtectedRoute'

function Secret() {
  return <p>Secret content</p>
}

function TestRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/pokemon/1/edit"
        element={
          <ProtectedRoute>
            <Secret />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}

describe('ProtectedRoute', () => {
  it('redirects to login when unauthenticated', () => {
    renderWithProviders(<TestRoutes />, { route: '/pokemon/1/edit' })

    expect(screen.getByRole('heading', { name: /sign in/i })).toBeInTheDocument()
    expect(screen.queryByText('Secret content')).not.toBeInTheDocument()
  })

  it('renders children when authenticated', () => {
    setAccessToken('token')
    renderWithProviders(<TestRoutes />, { route: '/pokemon/1/edit' })

    expect(screen.getByText('Secret content')).toBeInTheDocument()
  })
})
