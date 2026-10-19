import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/ApiError'
import { Button } from '../../components/Button'
import { validateAuthForm } from '../../lib/validate-auth'
import { fieldErrorsToMap } from '../../lib/validate-extras'
import { useAuth } from './useAuth'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
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
      await register({ username: username.trim(), password })
      navigate('/pokemon', { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        setFormError(err.detail ?? err.title)
        setFieldErrors(fieldErrorsToMap(err.errors))
      } else {
        setFormError('Could not register. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-md">
      <h1 className="text-2xl font-bold text-slate-900">Create account</h1>
      <form className="mt-6 space-y-4" onSubmit={onSubmit} noValidate>
        {formError ? (
          <p className="rounded-md bg-red-50 p-3 text-sm text-red-800" role="alert">
            {formError}
          </p>
        ) : null}
        <div>
          <label htmlFor="reg-username" className="block text-sm font-medium text-slate-700">
            Username
          </label>
          <input
            id="reg-username"
            name="username"
            autoComplete="username"
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            aria-invalid={fieldErrors.username ? true : undefined}
            aria-describedby={fieldErrors.username ? 'reg-username-error' : undefined}
          />
          {fieldErrors.username ? (
            <p id="reg-username-error" className="mt-1 text-sm text-red-700">
              {fieldErrors.username}
            </p>
          ) : null}
        </div>
        <div>
          <label htmlFor="reg-password" className="block text-sm font-medium text-slate-700">
            Password
          </label>
          <input
            id="reg-password"
            name="password"
            type="password"
            autoComplete="new-password"
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            aria-invalid={fieldErrors.password ? true : undefined}
            aria-describedby={fieldErrors.password ? 'reg-password-error' : undefined}
          />
          {fieldErrors.password ? (
            <p id="reg-password-error" className="mt-1 text-sm text-red-700">
              {fieldErrors.password}
            </p>
          ) : null}
        </div>
        <Button type="submit" disabled={submitting} className="w-full">
          {submitting ? 'Creating…' : 'Register'}
        </Button>
      </form>
      <p className="mt-4 text-sm text-slate-600">
        Already have an account?{' '}
        <Link className="text-indigo-700 underline" to="/login">
          Sign in
        </Link>
      </p>
    </div>
  )
}
