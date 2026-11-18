import { EstadoVacio } from '../componentes/ui/EstadoVacio'

export default function CatalogoEventos() {
  return (
    <section>
      <p className="text-sm font-semibold text-acento-500">PULSEPASS</p>
      <h1 className="mt-2 font-titulo text-3xl font-bold">Cartelera</h1>

      <div className="mt-8">
        <EstadoVacio
          titulo="Aún no hay eventos publicados"
          descripcion="Cuando conectemos la API, aquí aparecerán los próximos eventos."
        />
      </div>
    </section>
  )
}