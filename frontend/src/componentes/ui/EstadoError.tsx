import { Boton } from './Boton'

interface EstadoErrorProps {
  mensaje: string
  onReintentar?: () => void
}

export function EstadoError({
  mensaje,
  onReintentar,
}: EstadoErrorProps) {
  return (
    <section
      className="rounded-2xl border border-red-200 bg-red-50 px-6 py-10 text-center"
      role="alert"
    >
      <h2 className="font-titulo text-xl font-bold text-red-900">
        No pudimos cargar esta información
      </h2>
      <p className="mt-3 text-red-800">{mensaje}</p>
      {onReintentar ? (
        <Boton className="mt-6" variante="peligro" onClick={onReintentar}>
          Reintentar
        </Boton>
      ) : null}
    </section>
  )
}