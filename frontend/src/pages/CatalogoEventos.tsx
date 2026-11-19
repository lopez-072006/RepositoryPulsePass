import { Cargando } from '../componentes/ui/Cargando'
import { EstadoError } from '../componentes/ui/EstadoError'
import { EstadoVacio } from '../componentes/ui/EstadoVacio'
import { TarjetaEvento } from '../features/eventos/TarjetaEvento'
import { useEventosPublicados } from '../features/eventos/hooks'
import { useSearchParams } from 'react-router'
import { categoriasEvento } from '../features/eventos/esquemas'

export default function CatalogoEventos() {
  const { data, error, isError, isPending, refetch } = useEventosPublicados()
  const [params, setParams] = useSearchParams()
  const texto = params.get('q') ?? ''
  const categoria = params.get('categoria') ?? ''
  const actualizar = (key: 'q' | 'categoria', value: string) => { const siguiente = new URLSearchParams(params); if (value) siguiente.set(key, value); else siguiente.delete(key); setParams(siguiente) }

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

  const eventos = data.filter((evento) => (!categoria || evento.category === categoria) && (!texto || `${evento.name} ${evento.venueName} ${evento.city}`.toLocaleLowerCase().includes(texto.toLocaleLowerCase())))
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
      <p className="mt-3 text-slate-600">{eventos.length} eventos publicados</p>
      <div className="mt-6 grid gap-3 sm:grid-cols-[1fr_auto]"><label className="sr-only" htmlFor="buscar-evento">Buscar eventos</label><input id="buscar-evento" value={texto} onChange={(event) => actualizar('q', event.target.value)} className="rounded-xl border border-slate-300 bg-white px-4 py-3" placeholder="Buscar por evento, sede o ciudad" /><label className="sr-only" htmlFor="categoria-evento">Categoría</label><select id="categoria-evento" value={categoria} onChange={(event) => actualizar('categoria', event.target.value)} className="rounded-xl border border-slate-300 bg-white px-4 py-3"><option value="">Todas las categorías</option>{categoriasEvento.map((item) => <option key={item} value={item}>{item}</option>)}</select></div>

      {eventos.length === 0 ? <div className="mt-8"><EstadoVacio titulo="No encontramos eventos" descripcion="Prueba con otra búsqueda o categoría." /></div> : <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {eventos.map((evento) => (
          <TarjetaEvento key={evento.id} evento={evento} />
        ))}
      </div>}
    </section>
  )
}
