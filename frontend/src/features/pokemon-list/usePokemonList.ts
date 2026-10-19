import { keepPreviousData, useQuery } from '@tanstack/react-query'
import * as pokemonApi from '../../api/pokemon'
import type { PageSize } from '../../lib/pagination-url'

export function pokemonListQueryKey(limit: number, offset: number) {
  return ['pokemon', 'list', { limit, offset }] as const
}

export function usePokemonList(limit: PageSize, offset: number) {
  return useQuery({
    queryKey: pokemonListQueryKey(limit, offset),
    queryFn: () => pokemonApi.listPokemon({ limit, offset }),
    placeholderData: keepPreviousData,
  })
}
