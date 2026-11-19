import { getJson, patchJson, postJson } from '../../lib/http'
import { ticketSchema, ticketsSchema, type Ticket } from './esquemas'

const auth = (token: string) => ({ headers: { Authorization: `Bearer ${token}` } })
export const obtenerMisTickets = (email: string, token: string): Promise<Ticket[]> => getJson(`/api/tickets/by-user?email=${encodeURIComponent(email)}`, ticketsSchema, auth(token))
export const cancelarTicket = (code: string, token: string): Promise<Ticket> => patchJson(`/api/tickets/${code}/cancel`, ticketSchema, auth(token))
export const comprarTicket = (eventCode: string, type: Ticket['type'], email: string, token: string): Promise<Ticket> => postJson('/api/tickets', { eventCode, type, userEmail: email }, ticketSchema, auth(token))
export const obtenerTicket = (code: string, token: string): Promise<Ticket> => getJson(`/api/tickets/${code}`, ticketSchema, auth(token))
export const usarTicket = (code: string, token: string): Promise<Ticket> => patchJson(`/api/tickets/${code}/use`, ticketSchema, auth(token))
export const obtenerTicketsPagados = (eventCode: string, token: string): Promise<Ticket[]> => getJson(`/api/events/${eventCode}/tickets/paid`, ticketsSchema, auth(token))
