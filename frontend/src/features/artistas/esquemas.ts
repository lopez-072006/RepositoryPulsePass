import { z } from 'zod'

export const artistaSchema = z.object({
  id: z.number().int(),
  stageName: z.string().min(1),
  country: z.string().min(1),
  genre: z.string().min(1),
  active: z.boolean(),
})

export const artistasActivosSchema = z.array(artistaSchema)

export type Artista = z.infer<typeof artistaSchema>