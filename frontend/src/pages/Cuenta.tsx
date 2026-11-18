import { useAuth } from '../contextos/AuthContext'
import { Tarjeta } from '../componentes/ui/Tarjeta'

export default function Cuenta() {
  const { sesion } = useAuth()
  if (sesion.estado === 'anonima') return null
  const { usuario } = sesion
  return <section className="mx-auto max-w-xl"><h1 className="font-titulo text-3xl font-bold">Mi cuenta</h1><Tarjeta className="mt-6 space-y-3"><p><b>Usuario:</b> {usuario.username}</p><p><b>Correo:</b> {usuario.email}</p><p><b>Rol:</b> {usuario.roles.join(', ')}</p></Tarjeta></section>
}
