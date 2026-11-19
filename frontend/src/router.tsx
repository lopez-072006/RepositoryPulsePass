import { createBrowserRouter, RouterProvider } from 'react-router'
import { Layout } from './componentes/Layout'
import CatalogoEventos from './pages/CatalogoEventos'
import Inicio from './pages/Inicio'
import NoEncontrada from './pages/NoEncontrada'
import ListaArtistas from './pages/ListaArtistas'
import ListaSedes from './pages/ListaSedes'
import Login from './pages/Login'
import Registro from './pages/Registro'
import DetalleEvento from './pages/DetalleEvento'
import SinPermisos from './pages/SinPermisos'
import { RutaPorRol, RutaProtegida } from './componentes/RutaProtegida'

const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    children: [
      { index: true, Component: Inicio },
      { path: 'eventos', Component: CatalogoEventos },
      { path: 'eventos/:eventCode', Component: DetalleEvento },
      { path: '*', Component: NoEncontrada },
      { path: 'artistas', Component: ListaArtistas },
      { path: 'artistas/:id', lazy: async () => ({ Component: (await import('./pages/DetalleArtista')).default }) },
      { path: 'sedes', Component: ListaSedes },
      { path: 'sedes/:code', lazy: async () => ({ Component: (await import('./pages/DetalleSede')).default }) },
      { path: 'login', Component: Login },
      { path: 'registro', Component: Registro },
      { path: '403', Component: SinPermisos },
      {
        Component: RutaProtegida,
        children: [
          { path: 'cuenta', lazy: async () => ({ Component: (await import('./pages/Cuenta')).default }) },
          {
            Component: () => <RutaPorRol roles={['USER']} />,
            children: [{ path: 'mis-tickets', lazy: async () => ({ Component: (await import('./pages/MisTickets')).default }) }, { path: 'mis-tickets/:ticketCode', lazy: async () => ({ Component: (await import('./pages/DetalleTicket')).default }) }],
          },
          { Component: () => <RutaPorRol roles={['STAFF', 'ADMIN']} />, children: [{ path: 'staff/validacion', lazy: async () => ({ Component: (await import('./pages/ValidacionIngreso')).default }) }] },
          { Component: () => <RutaPorRol roles={['ADMIN']} />, children: [{ path: 'admin/eventos', lazy: async () => ({ Component: (await import('./pages/AdminEventos')).default }) }] },
        ],
      },
    ],
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
