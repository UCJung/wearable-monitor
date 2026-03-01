import { useState } from 'react'
import { useReturnDevice } from '@/hooks/useDevices'
import Modal, { ModalHeader, ModalFooter } from '@/components/ui/Modal'
import Button from '@/components/ui/Button'

interface ReturnDeviceModalProps {
  isOpen: boolean
  onClose: () => void
  assignmentId: number
  patientLabel: string
  serialNumber: string
}

export default function ReturnDeviceModal({ isOpen, onClose, assignmentId, patientLabel, serialNumber }: ReturnDeviceModalProps) {
  const [endDate] = useState(new Date().toISOString().slice(0, 10))
  const mutation = useReturnDevice()

  const handleConfirm = async () => {
    await mutation.mutateAsync({ id: assignmentId, data: { endDate } })
    onClose()
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} width={360}>
      <ModalHeader title="장치 해제 확인" onClose={onClose} />
      <div style={{ padding: '22px 20px', textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
        <div style={{ fontSize: 36 }}>🔓</div>
        <div style={{ fontSize: 14, fontWeight: 700 }}>장치를 해제하시겠습니까?</div>
        <div style={{ fontSize: 12.5, color: 'var(--text-sub)', lineHeight: 1.6 }}>
          <strong>{patientLabel}</strong> 환자의<br />
          장치 <strong>{serialNumber}</strong> 할당을 해제합니다.<br /><br />
          해제 후 해당 장치는 <span style={{ color: 'var(--ok)', fontWeight: 600 }}>AVAILABLE</span> 상태로 변경되어<br />
          다른 환자에게 할당할 수 있습니다.
        </div>
      </div>
      <ModalFooter>
        <Button variant="outline" onClick={onClose}>취소</Button>
        <Button variant="danger" onClick={handleConfirm} loading={mutation.isPending}>
          🔓 해제 확인
        </Button>
      </ModalFooter>
    </Modal>
  )
}
