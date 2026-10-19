import { Link, Outlet } from 'react-router-dom'
import { Button } from '../components/Button'
import { useAuth } from '../features/auth/useAuth'

export function RootLayout() {
  const { isAuthenticated, logout } = useAuth()

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-white focus:px-3 focus:py-2 focus:shadow"
      >
        Skip to content
      </a>
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4">
          <Link to="/pokemon" className="text-lg font-bold text-indigo-700">
            Pokehub
          </Link>
          <nav aria-label="Account" className="flex items-center gap-3 text-sm">
            {isAuthenticated ? (
              <>
                <span className="text-slate-600">Signed in</span>
                <Button variant="ghost" onClick={logout}>
                  Log out
                </Button>
              </>
            ) : (
              <>
                <Link className="text-indigo-700 underline" to="/login">
                  Sign in
                </Link>
                <Link className="text-indigo-700 underline" to="/register">
                  Register
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main id="main-content" className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
