import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import * as authApi from '../../api/auth'
import * as pokemonApi from '../../api/pokemon'
import { AppRouter } from '../../routes/AppRouter'
import { renderWithProviders } from '../../test/test-utils'

describe('LoginPage', () => {
  beforeEach(() => {
    vi.spyOn(authApi, 'login').mockResolvedValue({
      accessToken: 'jwt',
      tokenType: 'Bearer',
      expiresIn: 3600,
    })
    vi.spyOn(authApi, 'register').mockResolvedValue({ id: '1', username: 'ash' })
    vi.spyOn(pokemonApi, 'listPokemon').mockResolvedValue({
      count: 0,
      next: null,
      previous: null,
      results: [],
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows validation messages for weak credentials', async () => {
    renderWithProviders(<AppRouter />, { route: '/login' })

    await userEvent.type(screen.getByLabelText(/username/i), 'ab')
    await userEvent.type(screen.getByLabelText(/^password$/i), 'short')
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByText(/3–30 characters/i)).toBeInTheDocument()
    expect(screen.getByText(/8–72 characters/i)).toBeInTheDocument()
  })

  it('submits valid credentials', async () => {
    renderWithProviders(<AppRouter />, { route: '/login' })

    await userEvent.type(screen.getByLabelText(/username/i), 'ash')
    await userEvent.type(screen.getByLabelText(/^password$/i), 'Pikachu1')
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByText(/Pokémon catalog/i)).toBeInTheDocument()
  })
})
