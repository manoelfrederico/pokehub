import { useMutation, useQueryClient } from '@tanstack/react-query'
import * as pokemonApi from '../../api/pokemon'
import type { UpdateExtrasRequest } from '../../types/pokemon'
import { pokemonDetailQueryKey } from '../pokemon-detail/usePokemonDetail'

export function useUpdateExtras(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: UpdateExtrasRequest) => pokemonApi.updateExtras(id, body),
    onSuccess: (data) => {
      queryClient.setQueryData(pokemonDetailQueryKey(id), data)
      void queryClient.invalidateQueries({ queryKey: ['pokemon', 'list'] })
    },
  })
}
