import { getJson } from '../../lib/http'
import { sedesActivasSchema, type Sede } from './esquemas'

export function obtenerSedesActivas(): Promise<Sede[]> {
  return getJson('/api/venues/active', sedesActivasSchema)
}