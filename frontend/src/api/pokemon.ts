import { request } from './client'
import type {
  PokemonDetailsResponse,
  PokemonPageResponse,
  UpdateExtrasRequest,
} from '../types/pokemon'

export function listPokemon(params: {
  limit: number
  offset: number
}): Promise<PokemonPageResponse> {
  const search = new URLSearchParams({
    limit: String(params.limit),
    offset: String(params.offset),
  })
  return request<PokemonPageResponse>(`/pokemon?${search}`)
}

export function getPokemon(id: number): Promise<PokemonDetailsResponse> {
  return request<PokemonDetailsResponse>(`/pokemon/${id}`)
}

export function syncPokemon(id: number): Promise<PokemonDetailsResponse> {
  return request<PokemonDetailsResponse>(`/pokemon/${id}/sync`, {
    method: 'POST',
  })
}

export function unsyncPokemon(id: number): Promise<void> {
  return request<void>(`/pokemon/${id}/sync`, { method: 'DELETE' })
}

export function updateExtras(
  id: number,
  body: UpdateExtrasRequest,
): Promise<PokemonDetailsResponse> {
  return request<PokemonDetailsResponse>(`/pokemon/${id}/extras`, {
    method: 'PUT',
    body,
  })
}
