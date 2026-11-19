
import { createBrowserRouter, RouterProvider } from 'react-router'
import { Layout } from './componentes/Layout'
import { RutaPorRol, RutaProtegida } from './componentes/RutaProtegida'
import Inicio from './pages/Inicio'

const router = createBrowserRouter([
  {
    path: '/',
    Component: Layout,
    children: [
      // Página principal
      {
        index: true,
        Component: Inicio,
      },

      // Eventos
      {
        path: 'eventos',
        lazy: async () => ({
          Component: (await import('./pages/CatalogoEventos')).default,
        }),
      },
      {
        path: 'eventos/:eventCode',
        lazy: async () => ({
          Component: (await import('./pages/DetalleEvento')).default,
        }),
      },

      // Artistas
      {
        path: 'artistas',
        lazy: async () => ({
          Component: (await import('./pages/ListaArtistas')).default,
        }),
      },
      {
        path: 'artistas/:id',
        lazy: async () => ({
          Component: (await import('./pages/DetalleArtista')).default,
        }),
      },

      // Sedes
      {
        path: 'sedes',
        lazy: async () => ({
          Component: (await import('./pages/ListaSedes')).default,
        }),
      },
      {
        path: 'sedes/:code',
        lazy: async () => ({
          Component: (await import('./pages/DetalleSede')).default,
        }),
      },

      // Autenticación
      {
        path: 'login',
        lazy: async () => ({
          Component: (await import('./pages/Login')).default,
        }),
      },
      {
        path: 'registro',
        lazy: async () => ({
          Component: (await import('./pages/Registro')).default,
        }),
      },

      // Acceso denegado
      {
        path: '403',
        lazy: async () => ({
          Component: (await import('./pages/SinPermisos')).default,
        }),
      },

      // Rutas que requieren autenticación
      {
        Component: RutaProtegida,
        children: [
          {
            path: 'cuenta',
            lazy: async () => ({
              Component: (await import('./pages/Cuenta')).default,
            }),
          },

          // Rutas exclusivas para USER
          {
            Component: () => <RutaPorRol roles={['USER']} />,
            children: [
              {
                path: 'mis-tickets',
                lazy: async () => ({
                  Component: (await import('./pages/MisTickets')).default,
                }),
              },
              {
                path: 'mis-tickets/:ticketCode',
                lazy: async () => ({
                  Component: (await import('./pages/DetalleTicket')).default,
                }),
              },
              {
                path: 'compra/confirmacion/:ticketCode',
                lazy: async () => ({
                  Component: (
                    await import('./pages/ConfirmacionCompra')
                  ).default,
                }),
              },
            ],
          },

          // Rutas para STAFF y ADMIN
          {
            Component: () => (
              <RutaPorRol roles={['STAFF', 'ADMIN']} />
            ),
            children: [
              {
                path: 'staff/validacion',
                lazy: async () => ({
                  Component: (
                    await import('./pages/ValidacionIngreso')
                  ).default,
                }),
              },
            ],
          },

          // Rutas exclusivas para ADMIN
          {
            Component: () => <RutaPorRol roles={['ADMIN']} />,
            children: [
              {
                path: 'admin/eventos',
                lazy: async () => ({
                  Component: (await import('./pages/AdminEventos')).default,
                }),
              },
            ],
          },
        ],
      },

      // Ruta no encontrada
      {
        path: '*',
        lazy: async () => ({
          Component: (await import('./pages/NoEncontrada')).default,
        }),
      },
    ],
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
