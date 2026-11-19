import { getJson } from '../../lib/http'
import { sedeSchema, sedesActivasSchema, type Sede } from './esquemas'

export function obtenerSedesActivas(): Promise<Sede[]> {
  return getJson('/api/venues/active', sedesActivasSchema)
}

export function obtenerSede(code: string): Promise<Sede> {
  return getJson(`/api/venues/${encodeURIComponent(code)}`, sedeSchema)
}
