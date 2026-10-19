import { useQuery } from '@tanstack/react-query'
import * as pokemonApi from '../../api/pokemon'

export function pokemonDetailQueryKey(id: number) {
  return ['pokemon', 'detail', id] as const
}

export function usePokemonDetail(id: number) {
  return useQuery({
    queryKey: pokemonDetailQueryKey(id),
    queryFn: () => pokemonApi.getPokemon(id),
    enabled: Number.isFinite(id) && id >= 1,
  })
}
