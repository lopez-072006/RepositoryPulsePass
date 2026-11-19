import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../contextos/AuthContext'
import { RUTAS } from '../lib/rutas'

export function RutaProtegida() {
  const { sesion } = useAuth()
  const location = useLocation()

  if (sesion.estado === 'anonima') {
    return <Navigate replace state={{ desde: location.pathname }} to={RUTAS.LOGIN} />
  }

  return <Outlet />
}

interface RutaPorRolProps { roles: string[] }

export function RutaPorRol({ roles }: RutaPorRolProps) {
  const { sesion } = useAuth()

  if (sesion.estado === 'anonima') return <Navigate replace to={RUTAS.LOGIN} />
  if (!sesion.usuario.roles.some((rol) => roles.includes(rol))) {
    return <Navigate replace to="/403" />
  }

  return <Outlet />
}
