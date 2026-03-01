import type { ReactNode } from 'react'

type BadgeVariant = 'ok' | 'warn' | 'danger' | 'blue' | 'purple' | 'gray'

interface BadgeProps {
  variant: BadgeVariant
  dot?: boolean
  children: ReactNode
}

export default function Badge({ variant, dot = false, children }: BadgeProps) {
  return (
    <span className={`badge badge-${variant}`}>
      {dot && <span className="badge-dot" />}
      {children}
    </span>
  )
}
