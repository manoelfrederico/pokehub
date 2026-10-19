import { useCallback, useMemo, useState, type ReactNode } from 'react'
import { ToastContext } from './toast-context'

export function ToastProvider({ children }: { children: ReactNode }) {
  const [messages, setMessages] = useState<{ id: number; text: string }[]>([])

  const showToast = useCallback((text: string) => {
    const id = Date.now()
    setMessages((prev) => [...prev, { id, text }])
    window.setTimeout(() => {
      setMessages((prev) => prev.filter((m) => m.id !== id))
    }, 4000)
  }, [])

  const value = useMemo(() => ({ showToast }), [showToast])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className="pointer-events-none fixed bottom-4 right-4 z-50 flex flex-col gap-2"
        aria-live="polite"
        aria-atomic="true"
      >
        {messages.map((m) => (
          <div
            key={m.id}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white shadow-lg"
            role="status"
          >
            {m.text}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
