import { QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import * as pokemonApi from '../../api/pokemon'
import { createTestQueryClient } from '../../test/test-utils'
import { usePokemonList } from './usePokemonList'

describe('usePokemonList', () => {
  beforeEach(() => {
    vi.spyOn(pokemonApi, 'listPokemon').mockResolvedValue({
      count: 1,
      next: null,
      previous: null,
      results: [
        {
          id: 1,
          name: 'bulbasaur',
          sprite: null,
          category: 'Seed Pokémon',
          mass: 6.9,
          skills: [],
          synced: false,
        },
      ],
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('loads paginated data', async () => {
    const queryClient = createTestQueryClient()
    const { result } = renderHook(() => usePokemonList(20, 0), {
      wrapper: ({ children }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      ),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.results[0].name).toBe('bulbasaur')
  })
})
