import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'

export default function Inicio() {
  return (
    <section className="rounded-3xl bg-linear-to-r from-marca-600 to-acento-500 px-6 py-16 text-white sm:px-12">
      <p className="text-sm font-semibold tracking-wide">PULSEPASS</p>
      <h1 className="mt-4 max-w-2xl font-titulo text-4xl font-bold sm:text-5xl">
        Descubre los mejores eventos
      </h1>
      <p className="mt-5 max-w-xl text-lg text-white/90">
        Música, cultura, deporte y tecnología en un solo lugar.
      </p>
      <Link
        to={RUTAS.EVENTOS}
        className="mt-8 inline-flex rounded-xl bg-white px-5 py-3 font-semibold text-marca-700 hover:bg-marca-50"
      >
        Ver cartelera
      </Link>
    </section>
  )
}