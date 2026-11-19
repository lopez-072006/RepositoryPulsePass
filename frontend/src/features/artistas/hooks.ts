import { useQuery } from '@tanstack/react-query'
import { obtenerArtistasActivos } from './api'

export function useArtistasActivos() {
  return useQuery({
    queryKey: ['artistas', 'activos'],
    queryFn: obtenerArtistasActivos,
  })
}