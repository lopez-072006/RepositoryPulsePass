import { getJson } from '../../lib/http'
import {
  eventosPublicadosSchema,
  type EventoResumen,
} from './esquemas'

export function obtenerEventosPublicados(): Promise<EventoResumen[]> {
  return getJson('/api/events/published', eventosPublicadosSchema)
}