import type { ReactNode } from 'react'

interface EstadoVacioProps {
  titulo: string
  descripcion: string
  accion?: ReactNode
}

export function EstadoVacio({
  titulo,
  descripcion,
  accion,
}: EstadoVacioProps) {
  return (
    <section className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">
      <h2 className="font-titulo text-xl font-bold">{titulo}</h2>
      <p className="mx-auto mt-3 max-w-md text-slate-600">{descripcion}</p>
      {accion ? <div className="mt-6">{accion}</div> : null}
    </section>
  )
}