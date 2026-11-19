import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { EstadoVacio } from '../componentes/ui/EstadoVacio'
import { Tarjeta } from '../componentes/ui/Tarjeta'
import { useArtistasActivos } from '../features/artistas/hooks'
import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'

export default function ListaArtistas() {
  const { data, error, isError, isPending, refetch } = useArtistasActivos()

  if (isPending) {
    return <Cargando mensaje="Cargando artistas…" />
  }

  if (isError) {
    const mensaje = error instanceof Error ? error.message : 'Ocurrió un error.'

    return <EstadoError mensaje={mensaje} onReintentar={() => void refetch()} />
  }

  const artistas = data ?? []

  if (artistas.length === 0) {
    return (
      <EstadoVacio
        titulo="No hay artistas activos"
        descripcion="Vuelve más tarde para conocer a los artistas de PulsePass."
      />
    )
  }

  return (
    <section>
      <p className="text-sm font-semibold text-acento-500">PULSEPASS</p>
      <h1 className="mt-2 font-titulo text-3xl font-bold">Artistas</h1>

      <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {artistas.map((artista) => (
          <Tarjeta key={artista.id}>
            <p className="text-sm font-semibold text-marca-600">
              {artista.genre}
            </p>
            <h2 className="mt-2 font-titulo text-xl font-bold">
              {artista.stageName}
            </h2>
            <p className="mt-3 text-slate-600">{artista.country}</p>
            <Link to={RUTAS.DETALLE_ARTISTA(artista.id)} className="mt-4 inline-block text-sm font-semibold text-marca-600">Ver perfil →</Link>
          </Tarjeta>
        ))}
      </div>
    </section>
  )
}
