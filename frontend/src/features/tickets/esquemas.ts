import { z } from 'zod'

export const ticketSchema = z.object({
  id: z.number().int(), ticketCode: z.string(), type: z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT']),
  price: z.number(), status: z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED']), purchaseDate: z.string(),
  userEmail: z.string().email(), eventCode: z.string(), eventName: z.string(),
})
export const ticketsSchema = z.array(ticketSchema)
export type Ticket = z.infer<typeof ticketSchema>
