import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { Boton } from '../componentes/ui/Boton'
import { useAuth } from '../contextos/AuthContext'
import { loginSchema, type LoginInput } from '../features/auth/esquemas'
import { RUTAS } from '../lib/rutas'
import { useState } from 'react'

export default function Login() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const formulario = useForm<LoginInput>({
    resolver: zodResolver(loginSchema),
  })
  const [error, setError] = useState<string | null>(null)

  async function enviar(datos: LoginInput) {
    try {
      setError(null)
      await login(datos)
      navigate(RUTAS.INICIO)
    } catch (causa) {
      setError(
        causa instanceof Error ? causa.message : 'No fue posible iniciar sesión.',
      )
    }
  }

  return (
    <section className="mx-auto max-w-md">
      <h1 className="font-titulo text-3xl font-bold">Iniciar sesión</h1>

      <form className="mt-8 space-y-5" onSubmit={formulario.handleSubmit(enviar)}>
        <label className="block">
          <span className="font-medium">Usuario o correo</span>
          <input
            className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
            {...formulario.register('identifier')}
          />
          <small className="text-red-600">{formulario.formState.errors.identifier?.message}</small>
        </label>

        <label className="block">
          <span className="font-medium">Contraseña</span>
          <input
            type="password"
            className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-3"
            {...formulario.register('password')}
          />
          <small className="text-red-600">{formulario.formState.errors.password?.message}</small>
        </label>

        {error ? <p className="rounded-xl bg-red-50 p-3 text-red-700" role="alert">{error}</p> : null}

        <Boton className="w-full" cargando={formulario.formState.isSubmitting}>
          Ingresar
        </Boton>
      </form>
    </section>
  )
}