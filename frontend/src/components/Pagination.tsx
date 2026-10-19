import { Button } from './Button'
import {
  PAGE_SIZE_OPTIONS,
  pageFromOffset,
  totalPages,
  type PageSize,
} from '../lib/pagination-url'

type PaginationProps = {
  count: number
  limit: PageSize
  offset: number
  onLimitChange: (limit: PageSize) => void
  onOffsetChange: (offset: number) => void
}

export function Pagination({
  count,
  limit,
  offset,
  onLimitChange,
  onOffsetChange,
}: PaginationProps) {
  const page = pageFromOffset(offset, limit)
  const pages = totalPages(count, limit)
  const atStart = offset <= 0
  const atEnd = offset + limit >= count

  return (
    <nav
      className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"
      aria-label="Pagination"
    >
      <div className="flex items-center gap-2 text-sm text-slate-700">
        <label htmlFor="page-size">Per page</label>
        <select
          id="page-size"
          className="rounded-md border border-slate-300 bg-white px-2 py-1 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          value={limit}
          onChange={(e) => onLimitChange(Number(e.target.value) as PageSize)}
        >
          {PAGE_SIZE_OPTIONS.map((size) => (
            <option key={size} value={size}>
              {size}
            </option>
          ))}
        </select>
        <span aria-live="polite">
          Page {page} of {pages} ({count} total)
        </span>
      </div>
      <div className="flex gap-2">
        <Button
          variant="secondary"
          disabled={atStart}
          onClick={() => onOffsetChange(Math.max(0, offset - limit))}
          aria-label="Previous page"
        >
          Previous
        </Button>
        <Button
          variant="secondary"
          disabled={atEnd}
          onClick={() => onOffsetChange(offset + limit)}
          aria-label="Next page"
        >
          Next
        </Button>
      </div>
    </nav>
  )
}
