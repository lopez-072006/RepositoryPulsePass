
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { useMutation, useQueryClient } from '@tanstack/react-query'

import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { Tarjeta } from '../componentes/ui/Tarjeta'
import { Boton } from '../componentes/ui/Boton'

import { useEvento } from '../features/eventos/hooks'
import { comprarTicket } from '../features/tickets/api'

import { useAuth } from '../contextos/AuthContext'
import { RUTAS } from '../lib/rutas'

type TipoTicket = 'GENERAL' | 'VIP' | 'BACKSTAGE' | 'STUDENT'

export default function DetalleEvento() {
  const { eventCode } = useParams()
  const navigate = useNavigate()
  const cliente = useQueryClient()

  const { sesion } = useAuth()

  const [tipo, setTipo] = useState<TipoTicket>('GENERAL')

  const {
    data,
    error,
    isError,
    isPending,
    refetch,
  } = useEvento(eventCode ?? '')

  const compra = useMutation({
    mutationFn: () => {
      if (sesion.estado === 'anonima' || !data) {
        throw new Error('Inicia sesión para comprar una entrada.')
      }

      return comprarTicket(
        data.eventCode,
        tipo,
        sesion.usuario.email,
        sesion.token,
      )
    },

    onSuccess: (ticket) => {
      // Actualizar la información de los tickets
      void cliente.invalidateQueries({
        queryKey: ['tickets'],
      })

      // Guardar el ticket consultado en caché
      cliente.setQueryData(
        ['ticket', ticket.ticketCode],
        ticket,
      )

      // Redirigir a la confirmación de compra
      navigate(
        RUTAS.CONFIRMACION_COMPRA(ticket.ticketCode),
      )
    },
  })

  // Estado de carga
  if (isPending) {
    return <Cargando mensaje="Cargando evento..." />
  }

  // Estado de error
  if (isError) {
    const mensaje =
      error instanceof Error
        ? error.message
        : 'Evento no disponible.'

    return (
      <EstadoError
        mensaje={mensaje}
        onReintentar={() => void refetch()}
      />
    )
  }

  if (!data) {
    return null
  }

  // Formatear fecha del evento
  const fecha = new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'full',
    timeStyle: 'short',
  }).format(new Date(data.eventDate))

  const puedeComprar =
    sesion.estado === 'autenticada' &&
    sesion.usuario.roles.includes('USER') &&
    data.status === 'PUBLISHED'

  return (
    <section>
      <Link
        to={RUTAS.EVENTOS}
        className="text-sm font-semibold text-marca-600 hover:text-marca-700"
      >
        ← Volver a la cartelera
      </Link>

      <div className="mt-6 grid gap-8 lg:grid-cols-[2fr_1fr]">
        {/* Información del evento */}
        <article>
          <p className="font-mono text-sm text-slate-500">
            {data.eventCode}
          </p>

          <div className="mt-3 flex flex-wrap gap-3">
            <span className="rounded-full bg-violet-100 px-3 py-1 text-xs font-bold text-violet-800">
              {data.category}
            </span>

            <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800">
              {data.status}
            </span>
          </div>

          <h1 className="mt-5 font-titulo text-4xl font-bold">
            {data.name}
          </h1>

          <p className="mt-5 text-lg leading-8 text-slate-600">
            {data.description ??
              'Este evento no tiene descripción disponible.'}
          </p>

          {/* Artistas */}
          <div className="mt-8">
            <h2 className="font-titulo text-2xl font-bold">
              Artistas
            </h2>

            <div className="mt-4 flex flex-wrap gap-3">
              {data.artists.map((artista) => (
                <span
                  key={artista.id}
                  className="rounded-full bg-white px-4 py-2 text-sm font-semibold shadow-sm ring-1 ring-slate-200"
                >
                  {artista.stageName}
                </span>
              ))}
            </div>
          </div>
        </article>

        {/* Información y compra */}
        <aside>
          <Tarjeta>
            <h2 className="font-titulo text-xl font-bold">
              Información
            </h2>

            <dl className="mt-5 space-y-4 text-sm">
              <div>
                <dt className="text-slate-500">Fecha</dt>
                <dd className="mt-1 font-semibold">
                  {fecha}
                </dd>
              </div>

              <div>
                <dt className="text-slate-500">Sede</dt>
                <dd className="mt-1 font-semibold">
                  {data.venueName}
                </dd>
              </div>

              <div>
                <dt className="text-slate-500">
                  Edad mínima
                </dt>
                <dd className="mt-1 font-semibold">
                  {data.minimumAge} años
                </dd>
              </div>
            </dl>

            {/* Formulario de compra */}
            {puedeComprar && (
              <div className="mt-6 border-t pt-5">
                <label className="block text-sm font-medium">
                  Tipo de entrada

                  <select
                    className="mt-2 w-full rounded-lg border border-slate-300 p-2"
                    value={tipo}
                    onChange={(event) =>
                      setTipo(event.target.value as TipoTicket)
                    }
                    disabled={compra.isPending}
                  >
                    <option value="GENERAL">GENERAL</option>
                    <option value="VIP">VIP</option>
                    <option value="BACKSTAGE">BACKSTAGE</option>
                    <option value="STUDENT">STUDENT</option>
                  </select>
                </label>

                <Boton
                  className="mt-4 w-full"
                  cargando={compra.isPending}
                  onClick={() => compra.mutate()}
                >
                  Comprar entrada
                </Boton>

                {compra.isError && (
                  <p className="mt-3 text-sm text-red-700">
                    {compra.error instanceof Error
                      ? compra.error.message
                      : 'No fue posible completar la compra.'}
                  </p>
                )}
              </div>
            )}

            {/* Usuario no autenticado */}
            {sesion.estado === 'anonima' && (
              <Link
                className="mt-6 block text-center font-semibold text-marca-600"
                to={RUTAS.LOGIN}
              >
                Inicia sesión para comprar
              </Link>
            )}
          </Tarjeta>
        </aside>
      </div>
    </section>
  )
}

