import { useQuery } from '@tanstack/react-query'
import { obtenerSedesActivas } from './api'

export function useSedesActivas() {
  return useQuery({
    queryKey: ['sedes', 'activas'],
    queryFn: obtenerSedesActivas,
  })
}