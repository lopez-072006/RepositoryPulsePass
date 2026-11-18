import { z } from 'zod'

export const categoriasEvento = [
  'MUSIC',
  'SPORTS',
  'TECHNOLOGY',
  'EDUCATION',
  'CULTURE',
  'ENTERTAINMENT',
] as const

export const estadosEvento = [
  'DRAFT',
  'PUBLISHED',
  'SOLD_OUT',
  'CANCELLED',
  'FINISHED',
] as const

export const eventoResumenSchema = z.object({
  id: z.number().int(),
  eventCode: z.string().min(1),
  name: z.string().min(1),
  category: z.enum(categoriasEvento),
  status: z.enum(estadosEvento),
  eventDate: z.string().min(1),
  venueName: z.string().min(1),
  city: z.string().min(1),
})

export const eventosPublicadosSchema = z.array(eventoResumenSchema)

export type EventoResumen = z.infer<typeof eventoResumenSchema>
export type CategoriaEvento = z.infer<typeof eventoResumenSchema>['category']
export type EstadoEvento = z.infer<typeof eventoResumenSchema>['status']