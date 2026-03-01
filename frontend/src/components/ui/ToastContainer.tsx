import { useToastStore } from '@/stores/toastStore'
import Toast from './Toast'

export default function ToastContainer() {
  const { toasts, remove } = useToastStore()

  return (
    <div className="toast-wrap">
      {toasts.map((toast) => (
        <Toast key={toast.id} toast={toast} onClose={() => remove(toast.id)} />
      ))}
    </div>
  )
}
