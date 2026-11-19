import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { EstadoVacio } from '../componentes/ui/EstadoVacio'
import { Tarjeta } from '../componentes/ui/Tarjeta'
import { useSedesActivas } from '../features/sedes/hooks'
import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'

export default function ListaSedes() {
  const { data, error, isError, isPending, refetch } = useSedesActivas()

  if (isPending) {
    return <Cargando mensaje="Cargando sedes…" />
  }

  if (isError) {
    const mensaje = error instanceof Error ? error.message : 'Ocurrió un error.'

    return <EstadoError mensaje={mensaje} onReintentar={() => void refetch()} />
  }

  const sedes = data ?? []

  if (sedes.length === 0) {
    return (
      <EstadoVacio
        titulo="No hay sedes activas"
        descripcion="Vuelve más tarde para consultar las sedes disponibles."
      />
    )
  }

  return (
    <section>
      <p className="text-sm font-semibold text-acento-500">PULSEPASS</p>
      <h1 className="mt-2 font-titulo text-3xl font-bold">Sedes</h1>

      <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {sedes.map((sede) => (
          <Tarjeta key={sede.id}>
            <p className="font-mono text-xs text-slate-500">{sede.code}</p>
            <h2 className="mt-2 font-titulo text-xl font-bold">{sede.name}</h2>
            <p className="mt-3 text-slate-600">{sede.city}</p>
            <p className="mt-1 text-sm text-slate-600">{sede.address}</p>
            <p className="mt-4 text-sm font-semibold text-marca-600">
              Capacidad: {sede.capacity}
            </p>
            <Link to={RUTAS.DETALLE_SEDE(sede.code)} className="mt-4 inline-block text-sm font-semibold text-marca-600">Ver sede →</Link>
          </Tarjeta>
        ))}
      </div>
    </section>
  )
}
