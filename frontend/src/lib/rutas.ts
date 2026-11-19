
export const RUTAS = {
  INICIO: '/',
  EVENTOS: '/eventos',
  DETALLE_EVENTO: (eventCode: string) => `/eventos/${eventCode}`,

  ARTISTAS: '/artistas',
  DETALLE_ARTISTA: (id: string | number) => `/artistas/${id}`,

  SEDES: '/sedes',
  DETALLE_SEDE: (code: string) => `/sedes/${code}`,

  LOGIN: '/login',
  REGISTRO: '/registro',
  CUENTA: '/cuenta',

  MIS_TICKETS: '/mis-tickets',
  DETALLE_TICKET: (ticketCode: string) => `/mis-tickets/${ticketCode}`,
  CONFIRMACION_COMPRA: (ticketCode: string) =>
    `/compra/confirmacion/${ticketCode}`,

  STAFF_VALIDACION: '/staff/validacion',
  STAFF_TICKETS_EVENTO: (eventCode: string) =>
    `/staff/eventos/${eventCode}/tickets`,

  ADMIN_EVENTOS: '/admin/eventos',
  ADMIN_NUEVO_EVENTO: '/admin/eventos/nuevo',
  ADMIN_DETALLE_EVENTO: (eventCode: string) =>
    `/admin/eventos/${eventCode}`,

  SIN_PERMISOS: '/403',
} as const
