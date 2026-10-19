import { Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage } from '../features/auth/LoginPage'
import { RegisterPage } from '../features/auth/RegisterPage'
import { PokemonDetailPage } from '../features/pokemon-detail/PokemonDetailPage'
import { PokemonListPage } from '../features/pokemon-list/PokemonListPage'
import { EditExtrasPage } from '../features/sync-and-edit/EditExtrasPage'
import { ProtectedRoute } from './ProtectedRoute'
import { RootLayout } from './RootLayout'

export function AppRouter() {
  return (
    <Routes>
      <Route element={<RootLayout />}>
        <Route index element={<Navigate to="/pokemon" replace />} />
        <Route path="pokemon" element={<PokemonListPage />} />
        <Route path="pokemon/:id" element={<PokemonDetailPage />} />
        <Route
          path="pokemon/:id/edit"
          element={
            <ProtectedRoute>
              <EditExtrasPage />
            </ProtectedRoute>
          }
        />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="*" element={<Navigate to="/pokemon" replace />} />
      </Route>
    </Routes>
  )
}
