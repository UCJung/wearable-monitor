import type { Toast as ToastType } from '@/stores/toastStore'

const icons: Record<string, string> = {
  success: '✅',
  warn: '⚠️',
  info: 'ℹ️',
  danger: '🚫',
}

interface ToastProps {
  toast: ToastType
  onClose: () => void
}

export default function Toast({ toast, onClose }: ToastProps) {
  return (
    <div className={`toast toast-${toast.type}`}>
      <span style={{ fontSize: 16 }}>{icons[toast.type]}</span>
      <div style={{ flex: 1 }}>
        <p style={{ fontWeight: 600, fontSize: 13 }}>{toast.title}</p>
        {toast.message && (
          <p style={{ fontSize: 12, color: 'var(--text-sub)', marginTop: 2 }}>
            {toast.message}
          </p>
        )}
      </div>
      <button
        onClick={onClose}
        style={{
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          color: 'var(--text-sub)',
          fontSize: 14,
          lineHeight: 1,
        }}
      >
        ✕
      </button>
    </div>
  )
}
