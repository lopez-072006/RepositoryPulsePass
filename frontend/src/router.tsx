import { createBrowserRouter, RouterProvider } from 'react-router'
import { Layout } from './componentes/Layout'
import CatalogoEventos from './pages/CatalogoEventos'
import Inicio from './pages/Inicio'
import NoEncontrada from './pages/NoEncontrada'
import ListaArtistas from './pages/ListaArtistas'
import ListaSedes from './pages/ListaSedes'
import Login from './pages/Login'
import Registro from './pages/Registro'

const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    children: [
      { index: true, Component: Inicio },
      { path: 'eventos', Component: CatalogoEventos },
      { path: '*', Component: NoEncontrada },
      { path: 'artistas', Component: ListaArtistas },
      { path: 'sedes', Component: ListaSedes },
      { path: 'login', Component: Login },
      { path: 'registro', Component: Registro },
    ],
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}