import { useState } from 'react'
import { useDeviceList, useAssignDevice } from '@/hooks/useDevices'
import Modal, { ModalHeader, ModalBody, ModalFooter } from '@/components/ui/Modal'
import Button from '@/components/ui/Button'

interface AssignDeviceModalProps {
  isOpen: boolean
  onClose: () => void
  patientId: number
  patientLabel: string
}

export default function AssignDeviceModal({ isOpen, onClose, patientId, patientLabel }: AssignDeviceModalProps) {
  const [deviceId, setDeviceId] = useState<number>(0)
  const [startDate, setStartDate] = useState(new Date().toISOString().slice(0, 10))
  const [endDate, setEndDate] = useState('')
  const [memo, setMemo] = useState('')

  const { data: devicePage } = useDeviceList({ deviceStatus: 'ACTIVE', size: 100 })
  const availableDevices = devicePage?.content?.filter((d) => !d.assignedPatientName) ?? []

  const mutation = useAssignDevice()

  const handleSubmit = async () => {
    if (!deviceId) return
    await mutation.mutateAsync({
      patientId,
      deviceId,
      startDate,
      endDate: endDate || undefined,
    })
    onClose()
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <ModalHeader title="⌚ 장치 할당" onClose={onClose} />
      <ModalBody>
        <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
          <div className="form-label">환자</div>
          <input className="form-input" value={patientLabel} readOnly style={{ background: 'var(--tbl-header)', cursor: 'default' }} />
        </div>
        <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
          <div className="form-label">장치 선택 <span style={{ color: 'var(--danger)' }}>*</span></div>
          <select className="form-select" value={deviceId} onChange={(e) => setDeviceId(Number(e.target.value))} style={{ width: '100%' }}>
            <option value={0}>선택해 주세요</option>
            {availableDevices.map((d) => (
              <option key={d.id} value={d.id}>
                {d.serialNumber} {d.modelName} (AVAILABLE)
              </option>
            ))}
          </select>
        </div>
        <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
          <div className="form-label">모니터링 시작일 <span style={{ color: 'var(--danger)' }}>*</span></div>
          <input type="date" className="form-input" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
          <div className="form-label">모니터링 종료일</div>
          <input type="date" className="form-input" value={endDate} onChange={(e) => setEndDate(e.target.value)} placeholder="미입력 시 지속 모니터링" />
        </div>
        <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'start', gap: 10 }}>
          <div className="form-label" style={{ paddingTop: 6 }}>할당 목적</div>
          <textarea className="form-input" rows={2} value={memo} onChange={(e) => setMemo(e.target.value)} style={{ height: 'auto', padding: '7px 10px', resize: 'none' }} />
        </div>
      </ModalBody>
      <ModalFooter>
        <Button variant="outline" onClick={onClose}>취소</Button>
        <Button variant="primary" onClick={handleSubmit} loading={mutation.isPending} disabled={!deviceId}>
          ✓ 확인
        </Button>
      </ModalFooter>
    </Modal>
  )
}
