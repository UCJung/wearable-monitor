interface BatteryBarProps {
  level: number
}

export default function BatteryBar({ level }: BatteryBarProps) {
  const clamp = Math.max(0, Math.min(100, level))

  const color =
    clamp <= 20 ? 'var(--danger)' : clamp <= 30 ? '#f5a623' : 'var(--ok)'

  const isLow = clamp <= 20

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
      <div
        style={{
          width: 36,
          height: 10,
          background: '#e8eaed',
          borderRadius: 3,
          border: '1px solid #d0d3d8',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            width: `${clamp}%`,
            height: '100%',
            background: color,
            borderRadius: 2,
          }}
        />
      </div>
      <span
        style={{
          fontSize: 11,
          fontWeight: isLow ? 700 : 400,
          color: isLow ? 'var(--danger)' : 'var(--text)',
        }}
      >
        {clamp}%
      </span>
    </div>
  )
}
