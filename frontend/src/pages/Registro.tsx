import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { Boton } from '../componentes/ui/Boton'
import { registrarUsuario } from '../features/auth/api'
import { registroSchema, type RegistroInput } from '../features/auth/esquemas'
import { RUTAS } from '../lib/rutas'

export default function Registro() {
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const formulario = useForm<RegistroInput>({
    resolver: zodResolver(registroSchema),
    defaultValues: { phone: '', city: '' },
  })

  async function enviar(datos: RegistroInput) {
    try {
      setError(null)
      await registrarUsuario(datos)
      navigate(RUTAS.LOGIN)
    } catch (causa) {
      setError(
        causa instanceof Error ? causa.message : 'No fue posible crear la cuenta.',
      )
    }
  }

  return (
    <section className="mx-auto max-w-md">
      <h1 className="font-titulo text-3xl font-bold">Crear cuenta</h1>

      <form className="mt-8 grid gap-5" onSubmit={formulario.handleSubmit(enviar)}>
        {[
          ['username', 'Usuario', 'text'],
          ['email', 'Correo', 'email'],
          ['firstName', 'Nombre', 'text'],
          ['lastName', 'Apellido', 'text'],
          ['phone', 'Teléfono', 'text'],
          ['city', 'Ciudad', 'text'],
          ['birthDate', 'Fecha de nacimiento', 'date'],
          ['password', 'Contraseña', 'password'],
        ].map(([campo, etiqueta, tipo]) => (
          <label key={campo} className="block">
            <span className="font-medium">{etiqueta}</span>
            <input
              type={tipo}
              className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
              {...formulario.register(campo as keyof RegistroInput)}
            />
            <small className="text-red-600">
              {formulario.formState.errors[campo as keyof RegistroInput]?.message}
            </small>
          </label>
        ))}

        {error ? <p className="rounded-xl bg-red-50 p-3 text-red-700" role="alert">{error}</p> : null}

        <Boton className="w-full" cargando={formulario.formState.isSubmitting}>
          Crear cuenta
        </Boton>
      </form>
    </section>
  )
}