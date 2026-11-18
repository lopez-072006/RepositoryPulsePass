import { getJson } from '../../lib/http'
import {
  eventosPublicadosSchema,
  type EventoResumen,
} from './esquemas'

import {
  eventoDetalleSchema,
  type EventoDetalle,
} from './esquemas'

export function obtenerEventosPublicados(): Promise<EventoResumen[]> {
  return getJson('/api/events/published', eventosPublicadosSchema)
}

export function obtenerEvento(codigo: string): Promise<EventoDetalle> {
  return getJson(`/api/events/${codigo}`, eventoDetalleSchema)
}