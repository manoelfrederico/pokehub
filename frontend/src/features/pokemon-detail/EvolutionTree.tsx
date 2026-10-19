import { Link } from 'react-router-dom'
import { PokemonSprite } from '../../components/PokemonSprite'
import type { EvolutionNodeResponse } from '../../types/pokemon'

type EvolutionTreeProps = {
  node: EvolutionNodeResponse
  currentId: number
}

export function EvolutionTree({ node, currentId }: EvolutionTreeProps) {
  return (
    <ul className="flex flex-col gap-4" role="list">
      <EvolutionBranch node={node} currentId={currentId} />
    </ul>
  )
}

function EvolutionBranch({
  node,
  currentId,
}: {
  node: EvolutionNodeResponse
  currentId: number
}) {
  const isCurrent = node.id === currentId

  return (
    <li className="flex flex-col gap-3">
      <div
        className={`flex max-w-xs items-center gap-3 rounded-lg border p-3 ${
          isCurrent ? 'border-indigo-500 bg-indigo-50' : 'border-slate-200 bg-white'
        }`}
      >
        <div className="w-16 shrink-0">
          <PokemonSprite src={node.sprite} name={node.name} />
        </div>
        <div>
          {isCurrent ? (
            <span className="text-sm font-semibold capitalize text-indigo-900">
              {node.name}{' '}
              <span className="text-xs font-normal text-indigo-700">(current)</span>
            </span>
          ) : (
            <Link
              to={`/pokemon/${node.id}`}
              className="text-sm font-semibold capitalize text-indigo-700 underline"
            >
              {node.name}
            </Link>
          )}
        </div>
      </div>
      {node.evolvesTo.length > 0 ? (
        <ul
          className={`ml-4 flex flex-col gap-3 border-l-2 border-slate-200 pl-4 ${
            node.evolvesTo.length > 1 ? 'gap-4' : ''
          }`}
          role="list"
        >
          {node.evolvesTo.map((child) => (
            <EvolutionBranch key={child.id} node={child} currentId={currentId} />
          ))}
        </ul>
      ) : null}
    </li>
  )
}
