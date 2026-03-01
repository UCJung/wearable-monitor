import { type ReactNode, useEffect } from 'react'
import { createPortal } from 'react-dom'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  width?: number
  children: ReactNode
}

export default function Modal({ isOpen, onClose, width = 480, children }: ModalProps) {
  useEffect(() => {
    if (!isOpen) return
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [isOpen, onClose])

  if (!isOpen) return null

  return createPortal(
    <div className="modal-overlay" onClick={onClose}>
      <div
        className={`modal${width === 360 ? ' confirm-modal' : ''}`}
        style={width !== 480 && width !== 360 ? { width } : undefined}
        onClick={(e) => e.stopPropagation()}
      >
        {children}
      </div>
    </div>,
    document.body
  )
}

interface ModalHeaderProps {
  title: string
  onClose: () => void
}

export function ModalHeader({ title, onClose }: ModalHeaderProps) {
  return (
    <div className="modal-header">
      <span className="modal-title">{title}</span>
      <button
        onClick={onClose}
        style={{
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          fontSize: 18,
          color: 'var(--text-sub)',
          lineHeight: 1,
        }}
      >
        ✕
      </button>
    </div>
  )
}

export function ModalBody({ children }: { children: ReactNode }) {
  return <div className="modal-body">{children}</div>
}

export function ModalFooter({ children }: { children: ReactNode }) {
  return <div className="modal-footer">{children}</div>
}
