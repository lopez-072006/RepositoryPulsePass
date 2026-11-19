import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { Boton } from '../componentes/ui/Boton'
import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { Tarjeta } from '../componentes/ui/Tarjeta'
import { useAuth } from '../contextos/AuthContext'
import { cancelarTicket, obtenerTicket } from '../features/tickets/api'
import { RUTAS } from '../lib/rutas'

export default function DetalleTicket() {
  const { ticketCode } = useParams(); const { sesion } = useAuth(); const cache = useQueryClient()
  const token = sesion.estado === 'autenticada' ? sesion.token : ''
  const ticket = useQuery({ queryKey: ['ticket', ticketCode], queryFn: () => obtenerTicket(ticketCode ?? '', token), enabled: Boolean(ticketCode && token) })
  const cancelar = useMutation({ mutationFn: () => cancelarTicket(ticketCode ?? '', token), onSuccess: () => { void cache.invalidateQueries({ queryKey: ['tickets'] }); void cache.invalidateQueries({ queryKey: ['ticket', ticketCode] }) } })
  if (ticket.isPending) return <Cargando mensaje="Cargando ticket…" />
  if (ticket.isError) return <EstadoError mensaje={ticket.error instanceof Error ? ticket.error.message : 'Ticket no disponible.'} onReintentar={() => void ticket.refetch()} />
  if (!ticket.data) return null
  return <section className="mx-auto max-w-xl"><Link to={RUTAS.MIS_TICKETS} className="text-sm font-semibold text-marca-600">← Mis tickets</Link><Tarjeta className="mt-6"><p className="font-mono text-sm text-slate-500">{ticket.data.ticketCode}</p><h1 className="mt-2 font-titulo text-3xl font-bold">{ticket.data.eventName}</h1><dl className="mt-7 grid gap-4 sm:grid-cols-2"><div><dt className="text-sm text-slate-500">Tipo</dt><dd className="font-semibold">{ticket.data.type}</dd></div><div><dt className="text-sm text-slate-500">Estado</dt><dd className="font-semibold">{ticket.data.status}</dd></div><div><dt className="text-sm text-slate-500">Precio</dt><dd className="font-semibold">{new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(ticket.data.price)}</dd></div><div><dt className="text-sm text-slate-500">Compra</dt><dd className="font-semibold">{new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(ticket.data.purchaseDate))}</dd></div></dl>{ticket.data.status === 'PAID' ? <Boton className="mt-8" variante="peligro" cargando={cancelar.isPending} onClick={() => { if (window.confirm('¿Cancelar este ticket?')) cancelar.mutate() }}>Cancelar ticket</Boton> : null}{cancelar.isSuccess ? <p className="mt-4 text-emerald-700">Ticket cancelado.</p> : null}{cancelar.isError ? <p className="mt-4 text-red-700">{cancelar.error instanceof Error ? cancelar.error.message : 'No fue posible cancelar.'}</p> : null}</Tarjeta></section>
}
