import { QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import * as pokemonApi from '../../api/pokemon'
import { createTestQueryClient } from '../../test/test-utils'
import { useUpdateExtras } from './useUpdateExtras'

describe('useUpdateExtras', () => {
  beforeEach(() => {
    vi.spyOn(pokemonApi, 'updateExtras').mockResolvedValue({
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
      extras: { localizedName: 'x', region: null, tags: [] },
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('calls updateExtras mutation', async () => {
    const queryClient = createTestQueryClient()
    const { result } = renderHook(() => useUpdateExtras(1), {
      wrapper: ({ children }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      ),
    })

    await result.current.mutateAsync({
      localizedName: 'x',
      region: null,
      tags: [],
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(pokemonApi.updateExtras).toHaveBeenCalledWith(1, {
      localizedName: 'x',
      region: null,
      tags: [],
    })
  })
})
