import { getJson } from '../../lib/http'
import { artistasActivosSchema, type Artista } from './esquemas'

export function obtenerArtistasActivos(): Promise<Artista[]> {
  return getJson('/api/artists/active', artistasActivosSchema)
}