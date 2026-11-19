import type { HTMLAttributes, ReactNode } from 'react'
import clsx from 'clsx'

interface TarjetaProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
}

export function Tarjeta({ children, className, ...props }: TarjetaProps) {
  return (
    <div
      className={clsx(
        'rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200',
        className,
      )}
      {...props}
    >
      {children}
    </div>
  )
}