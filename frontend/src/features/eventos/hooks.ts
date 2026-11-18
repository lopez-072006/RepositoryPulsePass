import { useQuery } from '@tanstack/react-query'
import { obtenerEventosPublicados } from './api'

export function useEventosPublicados() {
  return useQuery({
    queryKey: ['eventos', 'publicados'],
    queryFn: obtenerEventosPublicados,
  })
}