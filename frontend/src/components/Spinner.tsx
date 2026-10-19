export function Spinner({ label = 'Loading' }: { label?: string }) {
  return (
    <div
      className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-indigo-200 border-t-indigo-600"
      role="status"
      aria-label={label}
    />
  )
}
