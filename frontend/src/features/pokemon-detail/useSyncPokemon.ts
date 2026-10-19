import { useMutation, useQueryClient } from '@tanstack/react-query'
import * as pokemonApi from '../../api/pokemon'
import { pokemonDetailQueryKey } from './usePokemonDetail'
export function useSyncPokemon(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => pokemonApi.syncPokemon(id),
    onSuccess: (data) => {
      queryClient.setQueryData(pokemonDetailQueryKey(id), data)
      void queryClient.invalidateQueries({ queryKey: ['pokemon', 'list'] })
    },
  })
}

export function useUnsyncPokemon(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => pokemonApi.unsyncPokemon(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: pokemonDetailQueryKey(id) })
      void queryClient.invalidateQueries({ queryKey: ['pokemon', 'list'] })
    },
  })
}
