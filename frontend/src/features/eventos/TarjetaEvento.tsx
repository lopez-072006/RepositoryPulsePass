import { Link } from 'react-router'
import { Tarjeta } from '../../componentes/ui/Tarjeta'
import { RUTAS } from '../../lib/rutas'
import type {
  CategoriaEvento,
  EstadoEvento,
  EventoResumen,
} from './esquemas'

interface TarjetaEventoProps {
  evento: EventoResumen
}

const clasePorCategoria: Record<CategoriaEvento, string> = {
  MUSIC: 'bg-violet-100 text-violet-800',
  SPORTS: 'bg-emerald-100 text-emerald-800',
  TECHNOLOGY: 'bg-sky-100 text-sky-800',
  EDUCATION: 'bg-amber-100 text-amber-800',
  CULTURE: 'bg-pink-100 text-pink-800',
  ENTERTAINMENT: 'bg-orange-100 text-orange-800',
}

const clasePorEstado: Record<EstadoEvento, string> = {
  DRAFT: 'bg-slate-200 text-slate-700',
  PUBLISHED: 'bg-emerald-100 text-emerald-800',
  SOLD_OUT: 'bg-amber-100 text-amber-800',
  CANCELLED: 'bg-red-100 text-red-800',
  FINISHED: 'bg-slate-300 text-slate-800',
}

export function TarjetaEvento({ evento }: TarjetaEventoProps) {
  const fecha = new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(evento.eventDate))

  return (
    <Link
      to={RUTAS.DETALLE_EVENTO(evento.eventCode)}
      className="block transition hover:-translate-y-1"
    >
      <Tarjeta>
        <div className="flex items-start justify-between gap-3">
          <span
            className={`rounded-full px-3 py-1 text-xs font-bold ${clasePorCategoria[evento.category]}`}
          >
            {evento.category}
          </span>
          <span
            className={`rounded-full px-3 py-1 text-xs font-bold ${clasePorEstado[evento.status]}`}
          >
            {evento.status}
          </span>
        </div>

        <p className="mt-5 font-mono text-xs text-slate-500">
          {evento.eventCode}
        </p>
        <h2 className="mt-1 font-titulo text-xl font-bold">{evento.name}</h2>
        <p className="mt-3 text-sm text-slate-600">{fecha}</p>
        <p className="mt-1 text-sm text-slate-600">
          {evento.venueName} · {evento.city}
        </p>
      </Tarjeta>
    </Link>
  )
}