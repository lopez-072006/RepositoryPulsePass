import type { ButtonHTMLAttributes } from 'react'
import clsx from 'clsx'

type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'fantasma'

interface BotonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: VarianteBoton
  cargando?: boolean
}

const clasesPorVariante: Record<VarianteBoton, string> = {
  primario: 'bg-marca-600 text-white hover:bg-marca-700',
  secundario: 'border border-marca-600 bg-white text-marca-700 hover:bg-marca-50',
  peligro: 'bg-red-600 text-white hover:bg-red-700',
  fantasma: 'text-slate-700 hover:bg-slate-100',
}

export function Boton({
  children,
  className,
  variante = 'primario',
  cargando = false,
  disabled,
  ...props
}: BotonProps) {
  return (
    <button
      className={clsx(
        'inline-flex min-h-11 items-center justify-center rounded-xl px-4 py-2 font-semibold transition focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600 disabled:cursor-not-allowed disabled:opacity-60',
        clasesPorVariante[variante],
        className,
      )}
      disabled={disabled || cargando}
      {...props}
    >
      {cargando ? 'Cargando…' : children}
    </button>
  )
}