import { fireEvent, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import * as pokemonApi from '../../api/pokemon'
import { setAccessToken } from '../../lib/auth-storage'
import { renderWithProviders } from '../../test/test-utils'
import { EditExtrasPage } from './EditExtrasPage'

const syncedDetail = {
  id: 1,
  name: 'bulbasaur',
  image: null,
  sprite: null,
  category: null,
  mass: 1,
  skills: [],
  stats: [],
  description: null,
  evolutionChain: { id: 1, name: 'bulbasaur', sprite: null, evolvesTo: [] },
  synced: true,
  extras: { localizedName: '', region: '', tags: [] },
}

describe('EditExtrasPage', () => {
  beforeEach(() => {
    vi.spyOn(pokemonApi, 'getPokemon').mockResolvedValue(syncedDetail)
    vi.spyOn(pokemonApi, 'updateExtras').mockResolvedValue(syncedDetail)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows client validation errors', async () => {
    setAccessToken('token')
    renderWithProviders(
      <Routes>
        <Route path="/pokemon/:id/edit" element={<EditExtrasPage />} />
      </Routes>,
      { route: '/pokemon/1/edit' },
    )

    const nameInput = await screen.findByLabelText(/localized name/i)
    fireEvent.change(nameInput, { target: { value: 'x'.repeat(101) } })
    await userEvent.click(screen.getByRole('button', { name: /^save$/i }))

    expect(await screen.findByText(/at most 100/i)).toBeInTheDocument()
  })
})
