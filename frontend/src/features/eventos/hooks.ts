import { useQuery } from '@tanstack/react-query'
import { obtenerEventosPublicados } from './api'

import { obtenerEvento } from './api'

export function useEvento(codigo: string) {
  return useQuery({
    queryKey: ['eventos', codigo],
    queryFn: () => obtenerEvento(codigo),
    enabled: codigo.length > 0,
  })
}

export function useEventosPublicados() {
  return useQuery({
    queryKey: ['eventos', 'publicados'],
    queryFn: obtenerEventosPublicados,
  })
}