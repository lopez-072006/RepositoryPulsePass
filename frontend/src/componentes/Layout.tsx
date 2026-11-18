import { NavLink, Outlet } from 'react-router'
import { RUTAS } from '../lib/rutas'

function enlaceClases(activo: boolean) {
  return activo
    ? 'font-semibold text-marca-600'
    : 'text-slate-600 hover:text-marca-600'
}

export function Layout() {
  return (
    <div className="flex min-h-screen flex-col bg-slate-50 font-cuerpo text-slate-900">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:rounded focus:bg-white focus:px-4 focus:py-2"
      >
        Saltar al contenido
      </a>

      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <nav
          aria-label="Principal"
          className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4"
        >
          <NavLink
            to={RUTAS.INICIO}
            className="font-titulo text-xl font-bold text-marca-600"
          >
            PulsePass
          </NavLink>

          <div className="flex items-center gap-6 text-sm font-medium">
            <NavLink
              end
              to={RUTAS.INICIO}
              className={({ isActive }) => enlaceClases(isActive)}
            >
              Inicio
            </NavLink>

            <NavLink
              to={RUTAS.EVENTOS}
              className={({ isActive }) => enlaceClases(isActive)}
            >
              Eventos
            </NavLink>

            <NavLink
              to={RUTAS.ARTISTAS}
              className={({ isActive }) => enlaceClases(isActive)}
            >
              Artistas
            </NavLink>

            <NavLink
              to={RUTAS.SEDES}
              className={({ isActive }) => enlaceClases(isActive)}
            >
              Sedes
            </NavLink>
          </div>
        </nav>
      </header>

      <main
        id="contenido"
        className="mx-auto w-full max-w-6xl flex-1 px-4 py-8"
      >
        <Outlet />
      </main>

      <footer className="border-t border-slate-200 bg-white">
        <div className="mx-auto max-w-6xl px-4 py-6 text-sm text-slate-600">
          © 2026 PulsePass · Caso de estudio académico
        </div>
      </footer>
    </div>
  )
}