import type { ReactNode } from 'react'

interface SummaryCardProps {
  icon: ReactNode
  iconBg: string
  label: string
  value: string | number
  sub?: string
}

export default function SummaryCard({ icon, iconBg, label, value, sub }: SummaryCardProps) {
  return (
    <div
      style={{
        background: 'var(--white)',
        border: '1px solid var(--gray-border)',
        borderRadius: 8,
        padding: '14px 16px',
        display: 'flex',
        alignItems: 'center',
        gap: 12,
      }}
    >
      <div
        style={{
          width: 40,
          height: 40,
          borderRadius: 8,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 18,
          background: iconBg,
        }}
      >
        {icon}
      </div>
      <div>
        <div style={{ fontSize: 11, color: 'var(--text-sub)' }}>{label}</div>
        <div style={{ fontSize: 22, fontWeight: 700, color: 'var(--text)' }}>{value}</div>
        {sub && <div style={{ fontSize: 10.5, color: 'var(--text-sub)' }}>{sub}</div>}
      </div>
    </div>
  )
}
