import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/ApiError'
import { Button } from '../../components/Button'
import { validateAuthForm } from '../../lib/validate-auth'
import { fieldErrorsToMap } from '../../lib/validate-extras'
import { useAuth } from './useAuth'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const sessionMessage = (location.state as { message?: string } | null)?.message
  const returnTo =
    (location.state as { returnTo?: string } | null)?.returnTo ?? '/pokemon'

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(
    sessionMessage ?? null,
  )
  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setFormError(null)
    const clientErrors = validateAuthForm(username, password)
    if (clientErrors.length > 0) {
      setFieldErrors(fieldErrorsToMap(clientErrors))
      return
    }
    setFieldErrors({})
    setSubmitting(true)
    try {
      await login({ username: username.trim(), password })
      navigate(returnTo, { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        setFormError(err.detail ?? err.title)
        setFieldErrors(fieldErrorsToMap(err.errors))
      } else {
        setFormError('Could not sign in. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-md">
      <h1 className="text-2xl font-bold text-slate-900">Sign in</h1>
      <form className="mt-6 space-y-4" onSubmit={onSubmit} noValidate>
        {formError ? (
          <p className="rounded-md bg-red-50 p-3 text-sm text-red-800" role="alert">
            {formError}
          </p>
        ) : null}
        <div>
          <label htmlFor="username" className="block text-sm font-medium text-slate-700">
            Username
          </label>
          <input
            id="username"
            name="username"
            autoComplete="username"
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            aria-invalid={fieldErrors.username ? true : undefined}
            aria-describedby={fieldErrors.username ? 'username-error' : undefined}
          />
          {fieldErrors.username ? (
            <p id="username-error" className="mt-1 text-sm text-red-700">
              {fieldErrors.username}
            </p>
          ) : null}
        </div>
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-slate-700">
            Password
          </label>
          <input
            id="password"
            name="password"
            type="password"
            autoComplete="current-password"
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            aria-invalid={fieldErrors.password ? true : undefined}
            aria-describedby={fieldErrors.password ? 'password-error' : undefined}
          />
          {fieldErrors.password ? (
            <p id="password-error" className="mt-1 text-sm text-red-700">
              {fieldErrors.password}
            </p>
          ) : null}
        </div>
        <Button type="submit" disabled={submitting} className="w-full">
          {submitting ? 'Signing in…' : 'Sign in'}
        </Button>
      </form>
      <p className="mt-4 text-sm text-slate-600">
        No account?{' '}
        <Link className="text-indigo-700 underline" to="/register">
          Register
        </Link>
      </p>
    </div>
  )
}
