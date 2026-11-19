import { getJson } from '../../lib/http'
import { artistaSchema, artistasActivosSchema, type Artista } from './esquemas'

export function obtenerArtistasActivos(): Promise<Artista[]> {
  return getJson('/api/artists/active', artistasActivosSchema)
}

export function obtenerArtista(id: string): Promise<Artista> {
  return getJson(`/api/artists/${id}`, artistaSchema)
}
