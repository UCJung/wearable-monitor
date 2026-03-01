import type { ButtonHTMLAttributes, ReactNode } from 'react'

type ButtonVariant = 'primary' | 'outline' | 'danger'
type ButtonSize = 'default' | 'sm'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
  loading?: boolean
  children: ReactNode
}

const variantClass: Record<ButtonVariant, string> = {
  primary: 'btn-primary',
  outline: 'btn-outline',
  danger: 'btn-danger',
}

const sizeStyle: Record<ButtonSize, React.CSSProperties> = {
  default: {},
  sm: { height: 26, fontSize: 11, padding: '0 8px' },
}

export default function Button({
  variant = 'primary',
  size = 'default',
  loading = false,
  disabled,
  children,
  style,
  ...rest
}: ButtonProps) {
  return (
    <button
      className={`btn ${variantClass[variant]}`}
      disabled={disabled || loading}
      style={{ ...sizeStyle[size], opacity: disabled || loading ? 0.6 : 1, ...style }}
      {...rest}
    >
      {loading ? '처리 중...' : children}
    </button>
  )
}
