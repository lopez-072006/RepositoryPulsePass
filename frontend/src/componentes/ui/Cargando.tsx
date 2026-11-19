interface CargandoProps {
  mensaje?: string
}

export function Cargando({ mensaje = 'Cargando información…' }: CargandoProps) {
  return (
    <div className="flex items-center justify-center gap-3 py-12" role="status">
      <span
        aria-hidden="true"
        className="size-6 animate-spin rounded-full border-4 border-marca-100 border-t-marca-600"
      />
      <span className="text-slate-600">{mensaje}</span>
    </div>
  )
}