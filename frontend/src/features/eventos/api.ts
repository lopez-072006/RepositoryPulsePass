import { getJson, patchJson, postJson } from '../../lib/http'
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

export function obtenerEventosPorArtista(nombre: string): Promise<EventoResumen[]> {
  return getJson(`/api/events/by-artist?stageName=${encodeURIComponent(nombre)}`, eventosPublicadosSchema)
}

const auth = (token: string) => ({ headers: { Authorization: `Bearer ${token}` } })
export interface NuevoEvento { eventCode: string; name: string; description: string; category: string; eventDate: string; minimumAge: number; venueCode: string }
export const crearEvento = (data: NuevoEvento, token: string) => postJson('/api/events', data, eventoDetalleSchema, auth(token))
export const publicarEvento = (code: string, token: string) => patchJson(`/api/events/${code}/publish`, eventoDetalleSchema, auth(token))
export const asociarArtista = (code: string, artistId: number, token: string) => postJson(`/api/events/${code}/artists/${artistId}`, {}, eventoDetalleSchema, auth(token))
