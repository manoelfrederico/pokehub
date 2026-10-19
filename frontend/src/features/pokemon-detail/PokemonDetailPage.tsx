import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/ApiError'
import { Button } from '../../components/Button'
import { ConfirmDialog } from '../../components/ConfirmDialog'
import { ErrorState } from '../../components/ErrorState'
import { SkillChips } from '../../components/SkillChips'
import { Spinner } from '../../components/Spinner'
import { useToast } from '../../components/useToast'
import { useAuth } from '../auth/useAuth'
import { EvolutionTree } from './EvolutionTree'
import { StatsTable } from './StatsTable'
import { usePokemonDetail } from './usePokemonDetail'
import { useSyncPokemon, useUnsyncPokemon } from './useSyncPokemon'

export function PokemonDetailPage() {
  const { id: idParam } = useParams()
  const id = Number(idParam)
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const { showToast } = useToast()
  const [confirmUnsync, setConfirmUnsync] = useState(false)
  const [statusMessage, setStatusMessage] = useState<string | null>(null)

  const { data, isLoading, isError, error, refetch } = usePokemonDetail(id)
  const syncMutation = useSyncPokemon(id)
  const unsyncMutation = useUnsyncPokemon(id)

  if (!Number.isFinite(id) || id < 1) {
    return <ErrorState message="Invalid Pokémon id." />
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-16">
        <Spinner label="Loading Pokémon" />
      </div>
    )
  }

  if (isError) {
    const message =
      error instanceof ApiError
        ? (error.detail ?? error.title)
        : error instanceof Error
          ? error.message
          : 'Failed to load Pokémon.'
    return <ErrorState message={message} onRetry={() => void refetch()} />
  }

  if (!data) {
    return null
  }

  const heroSrc = data.image ?? data.sprite

  async function handleSync() {
    setStatusMessage(null)
    try {
      await syncMutation.mutateAsync()
      showToast('Pokémon synced to your local copy.')
      setStatusMessage('Synced successfully.')
    } catch (err) {
      const msg =
        err instanceof ApiError
          ? (err.detail ?? err.title)
          : 'Sync failed.'
      setStatusMessage(msg)
    }
  }

  async function handleUnsync() {
    setConfirmUnsync(false)
    setStatusMessage(null)
    try {
      await unsyncMutation.mutateAsync()
      showToast('Local copy removed.')
      setStatusMessage('Local copy removed.')
      void refetch()
    } catch (err) {
      const msg =
        err instanceof ApiError
          ? (err.detail ?? err.title)
          : 'Could not remove local copy.'
      setStatusMessage(msg)
    }
  }

  const busy = syncMutation.isPending || unsyncMutation.isPending

  return (
    <article>
      <p className="mb-4">
        <Link to="/pokemon" className="text-sm text-indigo-700 underline">
          ← Back to catalog
        </Link>
      </p>

      <div className="grid gap-8 lg:grid-cols-2">
        <div className="flex flex-col items-center">
          <div className="aspect-square w-full max-w-sm overflow-hidden rounded-xl bg-slate-100">
            {heroSrc ? (
              <img
                src={heroSrc}
                alt={`${data.name} artwork`}
                className="h-full w-full object-contain"
                width={400}
                height={400}
              />
            ) : (
              <div className="flex h-full items-center justify-center text-slate-500">
                No image available
              </div>
            )}
          </div>
        </div>

        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-3xl font-bold capitalize text-slate-900">{data.name}</h1>
            {data.synced ? (
              <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-sm font-medium text-emerald-800">
                Synced
              </span>
            ) : null}
          </div>
          <p className="mt-1 text-slate-600">{data.category ?? 'Unknown category'}</p>
          <p className="mt-2 text-slate-800">
            <span className="font-medium">Mass:</span> {data.mass} kg
          </p>

          <section className="mt-6" aria-labelledby="skills-heading">
            <h2 id="skills-heading" className="text-lg font-semibold text-slate-900">
              Skills
            </h2>
            <div className="mt-2">
              <SkillChips skills={data.skills} />
            </div>
          </section>

          {isAuthenticated ? (
            <section className="mt-6" aria-labelledby="actions-heading">
              <h2 id="actions-heading" className="text-lg font-semibold text-slate-900">
                Your actions
              </h2>
              <div className="mt-3 flex flex-wrap gap-2">
                <Button disabled={busy} onClick={() => void handleSync()}>
                  {syncMutation.isPending ? 'Syncing…' : 'Sync'}
                </Button>
                {data.synced ? (
                  <>
                    <Button
                      variant="secondary"
                      disabled={busy}
                      onClick={() => navigate(`/pokemon/${id}/edit`)}
                    >
                      Edit extras
                    </Button>
                    <Button
                      variant="danger"
                      disabled={busy}
                      onClick={() => setConfirmUnsync(true)}
                    >
                      Remove local copy
                    </Button>
                  </>
                ) : null}
              </div>
              <p className="mt-2 min-h-[1.25rem] text-sm text-slate-600" aria-live="polite">
                {statusMessage}
              </p>
            </section>
          ) : null}
        </div>
      </div>

      <section className="mt-10" aria-labelledby="description-heading">
        <h2 id="description-heading" className="text-lg font-semibold text-slate-900">
          Description
        </h2>
        <p className="mt-2 text-slate-700">
          {data.description ?? 'No description available.'}
        </p>
      </section>

      <section className="mt-10" aria-labelledby="stats-heading">
        <h2 id="stats-heading" className="text-lg font-semibold text-slate-900">
          Base stats
        </h2>
        <div className="mt-3">
          <StatsTable stats={data.stats} />
        </div>
      </section>

      {data.synced && data.extras ? (
        <section className="mt-10" aria-labelledby="extras-heading">
          <h2 id="extras-heading" className="text-lg font-semibold text-slate-900">
            Local extras
          </h2>
          <dl className="mt-2 grid gap-2 text-sm text-slate-800 sm:grid-cols-2">
            <div>
              <dt className="font-medium">Localized name</dt>
              <dd>{data.extras.localizedName ?? '—'}</dd>
            </div>
            <div>
              <dt className="font-medium">Region</dt>
              <dd>{data.extras.region ?? '—'}</dd>
            </div>
            <div className="sm:col-span-2">
              <dt className="font-medium">Tags</dt>
              <dd>
                {data.extras.tags.length > 0
                  ? data.extras.tags.join(', ')
                  : '—'}
              </dd>
            </div>
          </dl>
        </section>
      ) : null}

      <section className="mt-10" aria-labelledby="evolution-heading">
        <h2 id="evolution-heading" className="text-lg font-semibold text-slate-900">
          Evolution
        </h2>
        <div className="mt-4">
          <EvolutionTree node={data.evolutionChain} currentId={data.id} />
        </div>
      </section>

      <ConfirmDialog
        open={confirmUnsync}
        title="Remove local copy?"
        message="This deletes the stored snapshot and extras for this Pokémon. You can sync again later."
        confirmLabel="Remove"
        onConfirm={() => void handleUnsync()}
        onCancel={() => setConfirmUnsync(false)}
      />
    </article>
  )
}
