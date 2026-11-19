import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'

export default function NoEncontrada() {
  return (
    <section className="py-20 text-center">
      <p className="text-sm font-semibold text-acento-500">ERROR 404</p>
      <h1 className="mt-2 font-titulo text-3xl font-bold">Página no encontrada</h1>
      <Link to={RUTAS.INICIO} className="mt-6 inline-block font-semibold text-marca-600">
        Volver al inicio
      </Link>
    </section>
  )
}