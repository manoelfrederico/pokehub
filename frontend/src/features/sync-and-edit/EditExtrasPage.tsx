import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../../api/ApiError'
import { ErrorState } from '../../components/ErrorState'
import { Spinner } from '../../components/Spinner'
import { usePokemonDetail } from '../pokemon-detail/usePokemonDetail'
import { EditExtrasForm } from './EditExtrasForm'

export function EditExtrasPage() {
  const { id: idParam } = useParams()
  const id = Number(idParam)

  const { data, isLoading, isError, error } = usePokemonDetail(id)

  if (!Number.isFinite(id) || id < 1) {
    return <ErrorState message="Invalid Pokémon id." />
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-16">
        <Spinner label="Loading" />
      </div>
    )
  }

  if (isError) {
    return (
      <ErrorState
        message={
          error instanceof ApiError
            ? (error.detail ?? error.title)
            : 'Failed to load Pokémon.'
        }
      />
    )
  }

  if (!data?.synced) {
    return (
      <ErrorState
        title="Not synced"
        message="Sync this Pokémon before editing extras."
      />
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <p className="mb-4">
        <Link to={`/pokemon/${id}`} className="text-sm text-indigo-700 underline">
          ← Back to {data.name}
        </Link>
      </p>
      <h1 className="text-2xl font-bold capitalize text-slate-900">
        Edit extras — {data.name}
      </h1>
      <EditExtrasForm key={data.id} pokemon={data} />
    </div>
  )
}
