import type { ReactNode } from 'react'

export interface Column<T> {
  key: string
  header: string
  width?: number | string
  render?: (row: T, index: number) => ReactNode
}

interface TableProps<T> {
  columns: Column<T>[]
  data: T[]
  rowKey: (row: T) => string | number
  warnRow?: (row: T) => boolean
  emptyText?: string
  title?: string
  totalCount?: number
  actions?: ReactNode
  footer?: ReactNode
}

export default function Table<T>({
  columns,
  data,
  rowKey,
  warnRow,
  emptyText = '데이터가 없습니다.',
  title,
  totalCount,
  actions,
  footer,
}: TableProps<T>) {
  return (
    <div className="table-panel">
      {(title || actions) && (
        <div
          style={{
            padding: '11px 16px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {title && <span style={{ fontSize: 13, fontWeight: 600 }}>{title}</span>}
            {totalCount !== undefined && (
              <span style={{ fontSize: 12, color: 'var(--text-sub)' }}>
                총 {totalCount.toLocaleString()}건
              </span>
            )}
          </div>
          {actions && <div style={{ display: 'flex', gap: 6 }}>{actions}</div>}
        </div>
      )}

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.key} style={col.width ? { width: col.width } : undefined}>
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                style={{
                  textAlign: 'center',
                  padding: '40px 0',
                  color: 'var(--text-sub)',
                  fontSize: 13,
                }}
              >
                {emptyText}
              </td>
            </tr>
          ) : (
            data.map((row, idx) => (
              <tr key={rowKey(row)} className={warnRow?.(row) ? 'warn-row' : undefined}>
                {columns.map((col) => (
                  <td key={col.key}>
                    {col.render
                      ? col.render(row, idx)
                      : String((row as Record<string, unknown>)[col.key] ?? '')}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>

      {footer}
    </div>
  )
}
