import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { EstadoVacio } from '../componentes/ui/EstadoVacio'
import { obtenerArtista } from '../features/artistas/api'
import { TarjetaEvento } from '../features/eventos/TarjetaEvento'
import { obtenerEventosPorArtista } from '../features/eventos/api'
import { RUTAS } from '../lib/rutas'

export default function DetalleArtista() {
  const { id } = useParams()
  const artista = useQuery({ queryKey: ['artista', id], queryFn: () => obtenerArtista(id ?? ''), enabled: Boolean(id) })
  const eventos = useQuery({ queryKey: ['eventos', 'artista', artista.data?.stageName], queryFn: () => obtenerEventosPorArtista(artista.data!.stageName), enabled: Boolean(artista.data) })
  if (artista.isPending) return <Cargando mensaje="Cargando artista…" />
  if (artista.isError) return <EstadoError mensaje={artista.error instanceof Error ? artista.error.message : 'Artista no disponible.'} onReintentar={() => void artista.refetch()} />
  if (!artista.data) return null
  return <section><Link to={RUTAS.ARTISTAS} className="text-sm font-semibold text-marca-600">← Artistas</Link><p className="mt-8 text-sm font-semibold text-acento-500">{artista.data.genre}</p><h1 className="mt-2 font-titulo text-4xl font-bold">{artista.data.stageName}</h1><p className="mt-3 text-lg text-slate-600">{artista.data.country}</p><h2 className="mt-10 font-titulo text-2xl font-bold">Próximos eventos</h2>{eventos.isPending ? <Cargando mensaje="Cargando eventos…" /> : eventos.isError ? <EstadoError mensaje="No fue posible cargar los eventos." onReintentar={() => void eventos.refetch()} /> : eventos.data?.length ? <div className="mt-5 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">{eventos.data.map((evento) => <TarjetaEvento key={evento.id} evento={evento} />)}</div> : <div className="mt-5"><EstadoVacio titulo="Sin eventos publicados" descripcion="Este artista no tiene eventos disponibles por ahora." /></div>}</section>
}
