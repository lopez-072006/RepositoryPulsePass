import { z } from 'zod'

export const loginSchema = z.object({
  identifier: z.string().min(1, 'Ingresa tu usuario o correo'),
  password: z.string().min(1, 'Ingresa tu contraseña'),
})

export const rolesSchema = z.enum(['USER', 'STAFF', 'ADMIN'])

export const authResponseSchema = z.object({
  accessToken: z.string().min(1),
  tokenType: z.string(),
  expiresIn: z.number(),
})

export const usuarioAutenticadoSchema = z.object({
  id: z.number().int(),
  username: z.string().min(1),
  email: z.string().email(),
  roles: z.array(rolesSchema),
})

export type LoginInput = z.infer<typeof loginSchema>
export type UsuarioAutenticado = z.infer<typeof usuarioAutenticadoSchema>
export type Rol = z.infer<typeof rolesSchema>

export const registroSchema = z.object({
  username: z.string().min(1, 'Ingresa un nombre de usuario'),
  email: z.string().email('Ingresa un correo válido'),
  firstName: z.string().min(1, 'Ingresa tu nombre'),
  lastName: z.string().min(1, 'Ingresa tu apellido'),
  phone: z.string(),
  city: z.string(),
  birthDate: z.string().min(1, 'Selecciona tu fecha de nacimiento'),
  password: z.string().min(12, 'La contraseña debe tener al menos 12 caracteres'),
})

export const usuarioSchema = z.object({
  id: z.number().int(),
  username: z.string(),
  email: z.string().email(),
  active: z.boolean(),
  firstName: z.string(),
  lastName: z.string(),
  phone: z.string().nullable(),
  city: z.string().nullable(),
  birthDate: z.string().nullable(),
})

export type RegistroInput = z.infer<typeof registroSchema>