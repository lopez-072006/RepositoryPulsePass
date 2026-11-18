import { NavLink, Outlet } from 'react-router'
import { useAuth } from '../contextos/AuthContext'
import { RUTAS } from '../lib/rutas'

function enlaceClases(activo: boolean) {
  return activo
    ? 'font-semibold text-marca-600'
    : 'text-slate-600 hover:text-marca-600'
}

export function Layout() {
  const { sesion, logout } = useAuth()
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
            {sesion.estado === 'autenticada' ? (
              <>
                <NavLink to={RUTAS.CUENTA} className={({ isActive }) => enlaceClases(isActive)}>Mi cuenta</NavLink>
                {sesion.usuario.roles.includes('USER') ? <NavLink to={RUTAS.MIS_TICKETS} className={({ isActive }) => enlaceClases(isActive)}>Mis tickets</NavLink> : null}
                <button className="rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-100" onClick={logout}>Salir</button>
              </>
            ) : (
              <>
                <NavLink to={RUTAS.LOGIN} className={({ isActive }) => enlaceClases(isActive)}>Ingresar</NavLink>
                <NavLink to={RUTAS.REGISTRO} className="rounded-lg bg-marca-600 px-3 py-1.5 text-white hover:bg-marca-700">Registrarse</NavLink>
              </>
            )}
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
