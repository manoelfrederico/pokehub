import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import * as pokemonApi from '../../api/pokemon'
import { renderWithProviders } from '../../test/test-utils'
import { PokemonListPage } from './PokemonListPage'

const samplePage = {
  count: 30,
  next: null,
  previous: null,
  results: [
    {
      id: 1,
      name: 'bulbasaur',
      sprite: null,
      category: 'Seed Pokémon',
      mass: 6.9,
      skills: [{ name: 'overgrow', hidden: true }],
      synced: false,
    },
  ],
}

describe('PokemonListPage', () => {
  beforeEach(() => {
    vi.spyOn(pokemonApi, 'listPokemon').mockResolvedValue(samplePage)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows loading then results', async () => {
    renderWithProviders(<PokemonListPage />, { route: '/pokemon?limit=20&offset=0' })

    expect(await screen.findByRole('heading', { name: /bulbasaur/i })).toBeInTheDocument()
    expect(screen.getByText('(hidden)')).toBeInTheDocument()
  })

  it('shows error with retry', async () => {
    vi.mocked(pokemonApi.listPokemon).mockRejectedValueOnce(new Error('boom'))
    renderWithProviders(<PokemonListPage />, { route: '/pokemon' })

    expect(await screen.findByRole('alert')).toHaveTextContent('boom')
    vi.mocked(pokemonApi.listPokemon).mockResolvedValue(samplePage)
    await userEvent.click(screen.getByRole('button', { name: /try again/i }))
    expect(await screen.findByRole('heading', { name: /bulbasaur/i })).toBeInTheDocument()
  })

  it('updates offset in the URL on next page', async () => {
    renderWithProviders(<PokemonListPage />, {
      route: '/pokemon?limit=10&offset=0',
    })

    await screen.findByRole('heading', { name: /bulbasaur/i })
    await userEvent.click(screen.getByRole('button', { name: /next page/i }))
    expect(pokemonApi.listPokemon).toHaveBeenLastCalledWith({
      limit: 10,
      offset: 10,
    })
  })
})
