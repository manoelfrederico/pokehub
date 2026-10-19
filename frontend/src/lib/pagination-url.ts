export const PAGE_SIZE_OPTIONS = [10, 20, 50] as const
export type PageSize = (typeof PAGE_SIZE_OPTIONS)[number]

export const DEFAULT_LIMIT: PageSize = 20
export const DEFAULT_OFFSET = 0

export function parseLimit(raw: string | null): PageSize {
  const n = Number(raw)
  if (PAGE_SIZE_OPTIONS.includes(n as PageSize)) {
    return n as PageSize
  }
  return DEFAULT_LIMIT
}

export function parseOffset(raw: string | null): number {
  const n = Number(raw)
  if (!Number.isFinite(n) || n < 0) {
    return DEFAULT_OFFSET
  }
  return Math.floor(n)
}

export function pageFromOffset(offset: number, limit: number): number {
  return Math.floor(offset / limit) + 1
}

export function totalPages(count: number, limit: number): number {
  return Math.max(1, Math.ceil(count / limit))
}
