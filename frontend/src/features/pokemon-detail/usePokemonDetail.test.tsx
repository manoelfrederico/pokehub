import { QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import * as pokemonApi from '../../api/pokemon'
import { createTestQueryClient } from '../../test/test-utils'
import { usePokemonDetail } from './usePokemonDetail'

describe('usePokemonDetail', () => {
  beforeEach(() => {
    vi.spyOn(pokemonApi, 'getPokemon').mockResolvedValue({
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
      synced: false,
      extras: null,
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('loads detail by id', async () => {
    const queryClient = createTestQueryClient()
    const { result } = renderHook(() => usePokemonDetail(1), {
      wrapper: ({ children }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      ),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.name).toBe('bulbasaur')
  })
})
