export type SkillResponse = {
  name: string
  hidden: boolean
}

export type StatResponse = {
  name: string
  baseValue: number
}

export type EvolutionNodeResponse = {
  id: number
  name: string
  sprite: string | null
  evolvesTo: EvolutionNodeResponse[]
}

export type PokemonExtrasResponse = {
  localizedName: string | null
  region: string | null
  tags: string[]
}

export type PokemonSummaryResponse = {
  id: number
  name: string
  sprite: string | null
  category: string | null
  mass: number
  skills: SkillResponse[]
  synced: boolean
}

export type PokemonPageResponse = {
  count: number
  next: string | null
  previous: string | null
  results: PokemonSummaryResponse[]
}

export type PokemonDetailsResponse = {
  id: number
  name: string
  image: string | null
  sprite: string | null
  category: string | null
  mass: number
  skills: SkillResponse[]
  stats: StatResponse[]
  description: string | null
  evolutionChain: EvolutionNodeResponse
  synced: boolean
  extras: PokemonExtrasResponse | null
}

export type UpdateExtrasRequest = {
  localizedName?: string | null
  region?: string | null
  tags?: string[]
}
