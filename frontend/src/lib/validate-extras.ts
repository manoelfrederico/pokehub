import type { FieldError } from '../types/problem'

export type ExtrasFormValues = {
  localizedName: string
  region: string
  tags: string[]
}

const MAX_FIELD = 100
const MAX_TAGS = 10
const MAX_TAG_LEN = 30

export function normalizeExtrasForm(values: ExtrasFormValues): {
  localizedName: string | null
  region: string | null
  tags: string[]
} {
  const localizedName = values.localizedName.trim()
  const region = values.region.trim()
  const tags = values.tags.map((t) => t.trim().toLowerCase()).filter(Boolean)
  const uniqueTags = [...new Set(tags)]
  return {
    localizedName: localizedName === '' ? null : localizedName,
    region: region === '' ? null : region,
    tags: uniqueTags,
  }
}

export function validateExtrasForm(values: ExtrasFormValues): FieldError[] {
  const errors: FieldError[] = []
  const name = values.localizedName.trim()
  const region = values.region.trim()

  if (name.length > MAX_FIELD) {
    errors.push({
      field: 'localizedName',
      message: `must be at most ${MAX_FIELD} characters`,
    })
  }
  if (region.length > MAX_FIELD) {
    errors.push({
      field: 'region',
      message: `must be at most ${MAX_FIELD} characters`,
    })
  }

  const normalized = values.tags.map((t) => t.trim().toLowerCase()).filter(Boolean)
  if (normalized.length > MAX_TAGS) {
    errors.push({
      field: 'tags',
      message: `must have at most ${MAX_TAGS} tags`,
    })
  }
  for (const tag of normalized) {
    if (tag.length < 1 || tag.length > MAX_TAG_LEN) {
      errors.push({
        field: 'tags',
        message: `each tag must be 1–${MAX_TAG_LEN} characters`,
      })
      break
    }
  }
  const unique = new Set(normalized)
  if (unique.size !== normalized.length) {
    errors.push({ field: 'tags', message: 'must not contain duplicates' })
  }

  return errors
}

export function fieldErrorsToMap(errors: FieldError[]): Record<string, string> {
  const map: Record<string, string> = {}
  for (const err of errors) {
    if (!map[err.field]) {
      map[err.field] = err.message
    }
  }
  return map
}
