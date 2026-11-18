import { z } from 'zod'

export class ApiError extends Error {
  public readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const errorResponseSchema = z.object({
  message: z.string().optional(),
})

async function requestJson<T>(
  url: string,
  schema: z.ZodType<T>,
  options: RequestInit = {},
): Promise<T> {
  const response = await fetch(url, options)
  const body: unknown = await response.json().catch(() => null)

  if (!response.ok) {
    const error = errorResponseSchema.safeParse(body)

    throw new ApiError(
      response.status,
      error.success && error.data.message
        ? error.data.message
        : 'No fue posible completar la solicitud.',
    )
  }

  const result = schema.safeParse(body)

  if (!result.success) {
    throw new ApiError(
      502,
      'La respuesta del servidor no tiene el formato esperado.',
    )
  }

  return result.data
}

export function getJson<T>(
  url: string,
  schema: z.ZodType<T>,
  options?: RequestInit,
): Promise<T> {
  return requestJson(url, schema, options)
}

export function postJson<T>(
  url: string,
  body: unknown,
  schema: z.ZodType<T>,
  options: RequestInit = {},
): Promise<T> {
  return requestJson(url, schema, {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options.headers },
    body: JSON.stringify(body),
  })
}

export function patchJson<T>(
  url: string,
  schema: z.ZodType<T>,
  options: RequestInit = {},
): Promise<T> {
  return requestJson(url, schema, {
    ...options,
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...options.headers },
  })
}
