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

export async function getJson<T>(
  url: string,
  schema: z.ZodType<T>,
): Promise<T> {
  const response = await fetch(url)
  const body: unknown = await response.json().catch(() => null)

  if (!response.ok) {
    const error = errorResponseSchema.safeParse(body)

    throw new ApiError(
      response.status,
      error.success && error.data.message
        ? error.data.message
        : 'No fue posible obtener la información.',
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