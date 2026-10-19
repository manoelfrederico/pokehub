import { useState, type FormEvent, type KeyboardEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/ApiError'
import { Button } from '../../components/Button'
import { useToast } from '../../components/useToast'
import {
  fieldErrorsToMap,
  normalizeExtrasForm,
  validateExtrasForm,
  type ExtrasFormValues,
} from '../../lib/validate-extras'
import type { PokemonDetailsResponse } from '../../types/pokemon'
import { useUpdateExtras } from './useUpdateExtras'

type EditExtrasFormProps = {
  pokemon: PokemonDetailsResponse
}

export function EditExtrasForm({ pokemon }: EditExtrasFormProps) {
  const navigate = useNavigate()
  const { showToast } = useToast()
  const mutation = useUpdateExtras(pokemon.id)

  const [values, setValues] = useState<ExtrasFormValues>(() => ({
    localizedName: pokemon.extras?.localizedName ?? '',
    region: pokemon.extras?.region ?? '',
    tags: [...(pokemon.extras?.tags ?? [])],
  }))
  const [tagInput, setTagInput] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)

  function addTag(raw: string) {
    const tag = raw.trim().toLowerCase()
    if (!tag) {
      return
    }
    setValues((prev) => {
      if (prev.tags.includes(tag)) {
        return prev
      }
      return { ...prev, tags: [...prev.tags, tag] }
    })
    setTagInput('')
  }

  function removeTag(tag: string) {
    setValues((prev) => ({ ...prev, tags: prev.tags.filter((t) => t !== tag) }))
  }

  function onTagKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') {
      e.preventDefault()
      addTag(tagInput)
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setFormError(null)
    const clientErrors = validateExtrasForm(values)
    if (clientErrors.length > 0) {
      setFieldErrors(fieldErrorsToMap(clientErrors))
      return
    }
    setFieldErrors({})
    try {
      await mutation.mutateAsync(normalizeExtrasForm(values))
      showToast('Extras saved.')
      navigate(`/pokemon/${pokemon.id}`)
    } catch (err) {
      if (err instanceof ApiError) {
        setFormError(err.detail ?? err.title)
        setFieldErrors(fieldErrorsToMap(err.errors))
      } else {
        setFormError('Could not save extras.')
      }
    }
  }

  return (
    <form className="mt-6 space-y-4" onSubmit={onSubmit} noValidate>
      {formError ? (
        <p className="rounded-md bg-red-50 p-3 text-sm text-red-800" role="alert">
          {formError}
        </p>
      ) : null}
      <div>
        <label htmlFor="localizedName" className="block text-sm font-medium text-slate-700">
          Localized name
        </label>
        <input
          id="localizedName"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          value={values.localizedName}
          onChange={(e) =>
            setValues((v) => ({ ...v, localizedName: e.target.value }))
          }
          disabled={mutation.isPending}
          aria-invalid={fieldErrors.localizedName ? true : undefined}
          aria-describedby={
            fieldErrors.localizedName ? 'localizedName-error' : undefined
          }
        />
        {fieldErrors.localizedName ? (
          <p id="localizedName-error" className="mt-1 text-sm text-red-700">
            {fieldErrors.localizedName}
          </p>
        ) : null}
      </div>
      <div>
        <label htmlFor="region" className="block text-sm font-medium text-slate-700">
          Region
        </label>
        <input
          id="region"
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          value={values.region}
          onChange={(e) => setValues((v) => ({ ...v, region: e.target.value }))}
          disabled={mutation.isPending}
          aria-invalid={fieldErrors.region ? true : undefined}
          aria-describedby={fieldErrors.region ? 'region-error' : undefined}
        />
        {fieldErrors.region ? (
          <p id="region-error" className="mt-1 text-sm text-red-700">
            {fieldErrors.region}
          </p>
        ) : null}
      </div>
      <div>
        <span id="tags-label" className="block text-sm font-medium text-slate-700">
          Tags
        </span>
        <div className="mt-2 flex flex-wrap gap-2" aria-labelledby="tags-label">
          {values.tags.map((tag) => (
            <span
              key={tag}
              className="inline-flex items-center gap-1 rounded-full bg-indigo-100 px-2 py-0.5 text-sm text-indigo-900"
            >
              {tag}
              <button
                type="button"
                className="rounded px-1 text-indigo-700 hover:bg-indigo-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                onClick={() => removeTag(tag)}
                aria-label={`Remove tag ${tag}`}
                disabled={mutation.isPending}
              >
                ×
              </button>
            </span>
          ))}
        </div>
        <input
          className="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          placeholder="Add tag and press Enter"
          value={tagInput}
          onChange={(e) => setTagInput(e.target.value)}
          onKeyDown={onTagKeyDown}
          disabled={mutation.isPending}
          aria-describedby={fieldErrors.tags ? 'tags-error' : undefined}
        />
        {fieldErrors.tags ? (
          <p id="tags-error" className="mt-1 text-sm text-red-700">
            {fieldErrors.tags}
          </p>
        ) : null}
      </div>
      <div className="flex gap-2">
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Saving…' : 'Save'}
        </Button>
        <Button
          type="button"
          variant="secondary"
          disabled={mutation.isPending}
          onClick={() => navigate(`/pokemon/${pokemon.id}`)}
        >
          Cancel
        </Button>
      </div>
    </form>
  )
}
