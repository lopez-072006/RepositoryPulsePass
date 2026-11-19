import { getJson, postJson } from '../../lib/http'
import {
  authResponseSchema,
  type LoginInput,
  usuarioAutenticadoSchema,
} from './esquemas'

export function iniciarSesion(datos: LoginInput) {
  return postJson('/api/auth/login', datos, authResponseSchema)
}

export function obtenerUsuarioAutenticado(token: string) {
  return getJson('/api/auth/me', usuarioAutenticadoSchema, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
}

import {
  type RegistroInput,
  usuarioSchema,
} from './esquemas'

export function registrarUsuario(datos: RegistroInput) {
  return postJson('/api/users', datos, usuarioSchema)
}