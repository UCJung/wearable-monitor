import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

export default function PrivateRoute() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const role = useAuthStore((s) => s.role)

  if (!isAuthenticated || role !== 'STAFF') {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}
