import { Link } from 'react-router-dom'
import { PokemonSprite } from '../../components/PokemonSprite'
import { SkillChips } from '../../components/SkillChips'
import type { PokemonSummaryResponse } from '../../types/pokemon'

type PokemonCardProps = {
  pokemon: PokemonSummaryResponse
}

export function PokemonCard({ pokemon }: PokemonCardProps) {
  return (
    <article className="flex flex-col rounded-lg border border-slate-200 bg-white p-4 shadow-sm transition hover:border-indigo-300">
      <Link to={`/pokemon/${pokemon.id}`} className="group flex flex-1 flex-col gap-3">
        <PokemonSprite src={pokemon.sprite} name={pokemon.name} />
        <div className="flex items-start justify-between gap-2">
          <h2 className="text-lg font-semibold capitalize text-slate-900 group-hover:text-indigo-700">
            {pokemon.name}
          </h2>
          {pokemon.synced ? (
            <span className="shrink-0 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-800">
              Synced
            </span>
          ) : null}
        </div>
        <p className="text-sm text-slate-600">
          {pokemon.category ?? 'Unknown category'}
        </p>
        <p className="text-sm text-slate-700">
          <span className="font-medium">Mass:</span> {pokemon.mass} kg
        </p>
        <SkillChips skills={pokemon.skills} />
      </Link>
    </article>
  )
}
