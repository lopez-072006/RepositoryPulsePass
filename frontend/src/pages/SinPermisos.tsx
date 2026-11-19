import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'

export default function SinPermisos() {
  return <section className="py-20 text-center"><p className="text-sm font-bold text-acento-500">ERROR 403</p><h1 className="mt-2 font-titulo text-3xl font-bold">No tienes permisos para esta página</h1><Link className="mt-6 inline-block font-semibold text-marca-600" to={RUTAS.INICIO}>Volver al inicio</Link></section>
}
