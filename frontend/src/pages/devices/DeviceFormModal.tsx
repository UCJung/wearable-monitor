import { useState, useEffect } from 'react'
import { useDeviceDetail, useCreateDevice, useUpdateDevice } from '@/hooks/useDevices'
import Modal, { ModalHeader, ModalBody, ModalFooter } from '@/components/ui/Modal'
import Button from '@/components/ui/Button'

interface DeviceFormModalProps {
  isOpen: boolean
  onClose: () => void
  deviceId?: number
}

export default function DeviceFormModal({ isOpen, onClose, deviceId }: DeviceFormModalProps) {
  const isEdit = !!deviceId
  const { data: device } = useDeviceDetail(deviceId ?? 0)

  const [serialNumber, setSerialNumber] = useState('')
  const [modelName, setModelName] = useState('')
  const [error, setError] = useState('')

  const createMutation = useCreateDevice()
  const updateMutation = useUpdateDevice(deviceId ?? 0)

  useEffect(() => {
    if (isEdit && device) {
      setSerialNumber(device.serialNumber)
      setModelName(device.modelName || '')
    } else if (!isEdit) {
      setSerialNumber('')
      setModelName('')
    }
    setError('')
  }, [isEdit, device, isOpen])

  const handleSubmit = async () => {
    if (!isEdit && !serialNumber.trim()) {
      setError('시리얼 번호를 입력해 주세요.')
      return
    }

    if (isEdit) {
      await updateMutation.mutateAsync({ modelName: modelName || undefined })
    } else {
      await createMutation.mutateAsync({ serialNumber: serialNumber.trim(), modelName: modelName || undefined })
    }
    onClose()
  }

  const isPending = createMutation.isPending || updateMutation.isPending

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <ModalHeader title={isEdit ? '장치 정보 수정' : '⌚ 장치 신규 등록'} onClose={onClose} />
      <ModalBody>
        <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
          <div className="form-label">시리얼 번호 {!isEdit && <span style={{ color: 'var(--danger)' }}>*</span>}</div>
          {isEdit ? (
            <input className="form-input" value={serialNumber} readOnly style={{ background: 'var(--tbl-header)', cursor: 'default' }} />
          ) : (
            <div>
              <input
                className={`form-input${error ? ' error' : ''}`}
                value={serialNumber}
                onChange={(e) => { setSerialNumber(e.target.value); setError('') }}
                placeholder="예: SN-W7-00412"
              />
              {error && <p style={{ fontSize: 11, color: 'var(--danger)', marginTop: 2 }}>{error}</p>}
            </div>
          )}
        </div>
        <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
          <div className="form-label">모델명</div>
          <input className="form-input" value={modelName} onChange={(e) => setModelName(e.target.value)} placeholder="예: Galaxy Watch 7" />
        </div>
      </ModalBody>
      <ModalFooter>
        <Button variant="outline" onClick={onClose}>취소</Button>
        <Button variant="primary" onClick={handleSubmit} loading={isPending}>
          {isEdit ? '수정' : '등록'}
        </Button>
      </ModalFooter>
    </Modal>
  )
}
