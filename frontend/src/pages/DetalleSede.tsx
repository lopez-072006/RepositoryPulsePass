import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { Tarjeta } from '../componentes/ui/Tarjeta'
import { obtenerSede } from '../features/sedes/api'
import { RUTAS } from '../lib/rutas'

export default function DetalleSede() {
  const { code } = useParams()
  const sede = useQuery({ queryKey: ['sede', code], queryFn: () => obtenerSede(code ?? ''), enabled: Boolean(code) })
  if (sede.isPending) return <Cargando mensaje="Cargando sede…" />
  if (sede.isError) return <EstadoError mensaje={sede.error instanceof Error ? sede.error.message : 'Sede no disponible.'} onReintentar={() => void sede.refetch()} />
  if (!sede.data) return null
  return <section className="mx-auto max-w-2xl"><Link to={RUTAS.SEDES} className="text-sm font-semibold text-marca-600">← Sedes</Link><Tarjeta className="mt-6"><p className="font-mono text-sm text-slate-500">{sede.data.code}</p><h1 className="mt-2 font-titulo text-4xl font-bold">{sede.data.name}</h1><dl className="mt-8 grid gap-5 sm:grid-cols-2"><div><dt className="text-sm text-slate-500">Ciudad</dt><dd className="font-semibold">{sede.data.city}</dd></div><div><dt className="text-sm text-slate-500">Capacidad</dt><dd className="font-semibold">{sede.data.capacity.toLocaleString('es-CO')} personas</dd></div><div className="sm:col-span-2"><dt className="text-sm text-slate-500">Dirección</dt><dd className="font-semibold">{sede.data.address}</dd></div></dl></Tarjeta></section>
}
