import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  iniciarSesion,
  obtenerUsuarioAutenticado,
} from '../features/auth/api'
import {
  type LoginInput,
  type UsuarioAutenticado,
} from '../features/auth/esquemas'

type Sesion =
  | { estado: 'anonima' }
  | { estado: 'autenticada'; token: string; usuario: UsuarioAutenticado }

interface AuthContextValue {
  sesion: Sesion
  login: (datos: LoginInput) => Promise<void>
  logout: () => void
}

const CLAVE_SESION = 'pulsepass.sesion'

const AuthContext = createContext<AuthContextValue | null>(null)

function leerSesion(): Sesion {
  const guardada = sessionStorage.getItem(CLAVE_SESION)

  if (!guardada) {
    return { estado: 'anonima' }
  }

  try {
    return JSON.parse(guardada) as Sesion
  } catch {
    sessionStorage.removeItem(CLAVE_SESION)
    return { estado: 'anonima' }
  }
}

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [sesion, setSesion] = useState<Sesion>(leerSesion)

  async function login(datos: LoginInput) {
    const respuesta = await iniciarSesion(datos)
    const usuario = await obtenerUsuarioAutenticado(respuesta.accessToken)

    const nuevaSesion: Sesion = {
      estado: 'autenticada',
      token: respuesta.accessToken,
      usuario,
    }

    sessionStorage.setItem(CLAVE_SESION, JSON.stringify(nuevaSesion))
    setSesion(nuevaSesion)
  }

  function logout() {
    sessionStorage.removeItem(CLAVE_SESION)
    setSesion({ estado: 'anonima' })
  }

  const value = useMemo(
    () => ({ sesion, login, logout }),
    [sesion],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const contexto = useContext(AuthContext)

  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de AuthProvider.')
  }

  return contexto
}