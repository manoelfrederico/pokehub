import { createContext } from 'react'

export type ToastContextValue = {
  showToast: (text: string) => void
}

export const ToastContext = createContext<ToastContextValue | null>(null)
