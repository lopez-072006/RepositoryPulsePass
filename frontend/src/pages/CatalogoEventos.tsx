import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { EstadoVacio } from '../componentes/ui/EstadoVacio'
import { TarjetaEvento } from '../features/eventos/TarjetaEvento'
import { useEventosPublicados } from '../features/eventos/hooks'

export default function CatalogoEventos() {
  const { data, error, isError, isPending, refetch } = useEventosPublicados()

  if (isPending) {
    return <Cargando mensaje="Cargando cartelera…" />
  }

  if (isError) {
    const mensaje =
      error instanceof Error
        ? error.message
        : 'Ocurrió un error inesperado.'

    return <EstadoError mensaje={mensaje} onReintentar={() => void refetch()} />
  }

  if (data.length === 0) {
    return (
      <EstadoVacio
        titulo="Aún no hay eventos publicados"
        descripcion="Vuelve más tarde para descubrir los próximos eventos."
      />
    )
  }

  return (
    <section>
      <p className="text-sm font-semibold text-acento-500">PULSEPASS</p>
      <h1 className="mt-2 font-titulo text-3xl font-bold">Cartelera</h1>
      <p className="mt-3 text-slate-600">{data.length} eventos publicados</p>

      <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {data.map((evento) => (
          <TarjetaEvento key={evento.id} evento={evento} />
        ))}
      </div>
    </section>
  )
}