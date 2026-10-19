import { screen } from '@testing-library/react'
import { Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import * as pokemonApi from '../../api/pokemon'
import { setAccessToken } from '../../lib/auth-storage'
import { renderWithProviders } from '../../test/test-utils'
import { PokemonDetailPage } from './PokemonDetailPage'

const detail = {
  id: 1,
  name: 'bulbasaur',
  image: null,
  sprite: null,
  category: 'Seed Pokémon',
  mass: 6.9,
  skills: [],
  stats: [{ name: 'hp', baseValue: 45 }],
  description: 'A seed Pokémon.',
  evolutionChain: {
    id: 1,
    name: 'bulbasaur',
    sprite: null,
    evolvesTo: [
      {
        id: 2,
        name: 'ivysaur',
        sprite: null,
        evolvesTo: [],
      },
    ],
  },
  synced: true,
  extras: { localizedName: null, region: 'Kanto', tags: [] },
}

function DetailRoute() {
  return (
    <Routes>
      <Route path="/pokemon/:id" element={<PokemonDetailPage />} />
    </Routes>
  )
}

describe('PokemonDetailPage', () => {
  beforeEach(() => {
    vi.spyOn(pokemonApi, 'getPokemon').mockResolvedValue(detail)
    vi.spyOn(pokemonApi, 'syncPokemon').mockResolvedValue(detail)
    vi.spyOn(pokemonApi, 'unsyncPokemon').mockResolvedValue(undefined)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('hides auth actions when logged out', async () => {
    renderWithProviders(<DetailRoute />, { route: '/pokemon/1' })

    expect(await screen.findByRole('heading', { name: /bulbasaur/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^sync$/i })).not.toBeInTheDocument()
  })

  it('shows sync actions when authenticated', async () => {
    setAccessToken('token')
    renderWithProviders(<DetailRoute />, { route: '/pokemon/1' })

    expect(await screen.findByRole('button', { name: /^sync$/i })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ivysaur/i })).toHaveAttribute('href', '/pokemon/2')
  })
})
