import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { EstadoVacio } from '../componentes/ui/EstadoVacio'
import { Boton } from '../componentes/ui/Boton'
import { Tarjeta } from '../componentes/ui/Tarjeta'
import { useAuth } from '../contextos/AuthContext'
import { cancelarTicket, obtenerMisTickets } from '../features/tickets/api'

export default function MisTickets() {
  const { sesion } = useAuth(); const cliente = useQueryClient()
  if (sesion.estado === 'anonima') return null
  const { token, usuario } = sesion
  const consulta = useQuery({ queryKey: ['tickets', usuario.email], queryFn: () => obtenerMisTickets(usuario.email, token) })
  const cancelar = useMutation({ mutationFn: (code: string) => cancelarTicket(code, token), onSuccess: () => void cliente.invalidateQueries({ queryKey: ['tickets', usuario.email] }) })
  if (consulta.isPending) return <Cargando mensaje="Cargando tus tickets…" />
  if (consulta.isError) return <EstadoError mensaje={consulta.error instanceof Error ? consulta.error.message : 'No fue posible cargar los tickets.'} onReintentar={() => void consulta.refetch()} />
  const tickets = consulta.data ?? []
  if (!tickets.length) return <EstadoVacio titulo="Aún no tienes tickets" descripcion="Tus entradas compradas aparecerán aquí." />
  return <section><h1 className="font-titulo text-3xl font-bold">Mis tickets</h1><div className="mt-6 grid gap-4 sm:grid-cols-2">{tickets.map((ticket) => <Tarjeta key={ticket.ticketCode}><p className="font-mono text-xs text-slate-500">{ticket.ticketCode}</p><h2 className="mt-2 font-titulo text-xl font-bold">{ticket.eventName}</h2><p className="mt-2">{ticket.type} · {ticket.status}</p><p className="mt-1 text-slate-600">{new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(ticket.price)}</p>{ticket.status === 'PAID' ? <Boton className="mt-4" variante="peligro" cargando={cancelar.isPending} onClick={() => cancelar.mutate(ticket.ticketCode)}>Cancelar</Boton> : null}</Tarjeta>)}</div></section>
}
