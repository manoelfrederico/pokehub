import { describe, expect, it } from 'vitest'
import { normalizeExtrasForm, validateExtrasForm } from './validate-extras'

describe('validateExtrasForm', () => {
  it('accepts valid values', () => {
    expect(
      validateExtrasForm({
        localizedName: 'Bulba',
        region: 'Kanto',
        tags: ['starter'],
      }),
    ).toEqual([])
  })

  it('rejects duplicate tags', () => {
    const errors = validateExtrasForm({
      localizedName: '',
      region: '',
      tags: ['a', 'A'],
    })
    expect(errors.some((e) => e.field === 'tags')).toBe(true)
  })

  it('normalizes blank strings to null', () => {
    expect(
      normalizeExtrasForm({ localizedName: '  ', region: '', tags: [' Grass '] }),
    ).toEqual({
      localizedName: null,
      region: null,
      tags: ['grass'],
    })
  })
})
