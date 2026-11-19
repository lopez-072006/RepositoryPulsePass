
import { Link, useParams } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { CheckCircle2 } from 'lucide-react'
import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { Tarjeta } from '../componentes/ui/Tarjeta'
import { useAuth } from '../contextos/AuthContext'
import { obtenerTicket } from '../features/tickets/api'
import { RUTAS } from '../lib/rutas'

export default function ConfirmacionCompra() {
  const { ticketCode } = useParams()
  const { sesion } = useAuth()

  const token =
    sesion.estado === 'autenticada' ? sesion.token : ''

  const ticket = useQuery({
    queryKey: ['ticket', ticketCode],
    queryFn: () => obtenerTicket(ticketCode ?? '', token),
    enabled: Boolean(ticketCode && token),
  })

  if (ticket.isPending) {
    return <Cargando mensaje="Verificando tu compra..." />
  }

  if (ticket.isError) {
    return (
      <EstadoError
        mensaje={
          ticket.error instanceof Error
            ? ticket.error.message
            : 'No fue posible verificar tu compra.'
        }
        onReintentar={() => void ticket.refetch()}
      />
    )
  }

  if (!ticket.data) return null

  const entrada = ticket.data

  const precio = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
  }).format(entrada.price)

  return (
    <section className="mx-auto max-w-2xl">
      <Tarjeta className="text-center">
        <CheckCircle2
          size={64}
          className="mx-auto text-emerald-600"
        />

        <h1 className="mt-5 font-titulo text-3xl font-bold">
          ¡Compra realizada!
        </h1>

        <p className="mt-3 text-slate-600">
          Tu entrada ha sido registrada correctamente.
        </p>

        <div className="mt-8 rounded-xl bg-slate-50 p-6 text-left">
          <p className="text-sm text-slate-500">
            Código de entrada
          </p>

          <p className="mt-1 break-all font-mono text-xl font-bold">
            {entrada.ticketCode}
          </p>

          <div className="mt-6 grid gap-5 sm:grid-cols-2">
            <div>
              <p className="text-sm text-slate-500">Evento</p>
              <p className="font-semibold">{entrada.eventName}</p>
            </div>

            <div>
              <p className="text-sm text-slate-500">Tipo de entrada</p>
              <p className="font-semibold">{entrada.type}</p>
            </div>

            <div>
              <p className="text-sm text-slate-500">Precio</p>
              <p className="font-semibold">{precio}</p>
            </div>

            <div>
              <p className="text-sm text-slate-500">Estado</p>
              <p className="font-semibold">{entrada.status}</p>
            </div>
          </div>
        </div>

        <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <Link
            to={RUTAS.MIS_TICKETS}
            className="rounded-lg bg-marca-600 px-5 py-3 text-center font-semibold text-white hover:bg-marca-700"
          >
            Ver mis entradas
          </Link>

          <Link
            to={RUTAS.EVENTOS}
            className="rounded-lg border border-slate-300 px-5 py-3 text-center font-semibold hover:bg-slate-50"
          >
            Volver a eventos
          </Link>
        </div>
      </Tarjeta>
    </section>
  )
}
