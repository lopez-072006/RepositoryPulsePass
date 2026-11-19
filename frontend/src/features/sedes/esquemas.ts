import { z } from 'zod'

export const sedeSchema = z.object({
  id: z.number().int(),
  code: z.string().min(1),
  name: z.string().min(1),
  city: z.string().min(1),
  address: z.string().min(1),
  capacity: z.number().int().nonnegative(),
  active: z.boolean(),
})

export const sedesActivasSchema = z.array(sedeSchema)

export type Sede = z.infer<typeof sedeSchema>