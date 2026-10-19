import { useSearchParams } from 'react-router-dom'
import { ErrorState } from '../../components/ErrorState'
import { Pagination } from '../../components/Pagination'
import {
  DEFAULT_OFFSET,
  parseLimit,
  parseOffset,
  type PageSize,
} from '../../lib/pagination-url'
import { PokemonCard } from './PokemonCard'
import { usePokemonList } from './usePokemonList'

function PokemonListSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {Array.from({ length: 8 }, (_, i) => (
        <div
          key={i}
          className="animate-pulse rounded-lg border border-slate-200 p-4"
          aria-hidden="true"
        >
          <div className="aspect-square rounded-md bg-slate-200" />
          <div className="mt-3 h-4 w-2/3 rounded bg-slate-200" />
          <div className="mt-2 h-3 w-full rounded bg-slate-100" />
        </div>
      ))}
    </div>
  )
}

export function PokemonListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const limit = parseLimit(searchParams.get('limit'))
  const offset = parseOffset(searchParams.get('offset'))

  const { data, isLoading, isError, error, refetch, isFetching } =
    usePokemonList(limit, offset)

  function updateParams(nextLimit: PageSize, nextOffset: number) {
    setSearchParams({
      limit: String(nextLimit),
      offset: String(nextOffset),
    })
  }

  return (
    <div>
      <div className="mb-6 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Pokémon catalog</h1>
          <p className="text-slate-600">Browse species from the PokéAPI catalog.</p>
        </div>
        {isFetching && !isLoading ? (
          <p className="text-sm text-slate-500" aria-live="polite">
            Updating…
          </p>
        ) : null}
      </div>

      {isLoading ? <PokemonListSkeleton /> : null}

      {isError ? (
        <ErrorState
          message={
            error instanceof Error ? error.message : 'Failed to load Pokémon.'
          }
          onRetry={() => void refetch()}
        />
      ) : null}

      {data && data.results.length === 0 ? (
        <p className="rounded-lg border border-slate-200 bg-slate-50 p-8 text-center text-slate-600">
          No Pokémon found for this page.
        </p>
      ) : null}

      {data && data.results.length > 0 ? (
        <>
          <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {data.results.map((pokemon) => (
              <li key={pokemon.id}>
                <PokemonCard pokemon={pokemon} />
              </li>
            ))}
          </ul>
          <div className="mt-8">
            <Pagination
              count={data.count}
              limit={limit}
              offset={offset}
              onLimitChange={(nextLimit) =>
                updateParams(nextLimit, DEFAULT_OFFSET)
              }
              onOffsetChange={(nextOffset) => updateParams(limit, nextOffset)}
            />
          </div>
        </>
      ) : null}
    </div>
  )
}
