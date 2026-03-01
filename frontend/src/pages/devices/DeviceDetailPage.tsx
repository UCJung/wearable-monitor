import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useDeviceDetail, useDeleteDevice } from '@/hooks/useDevices'
import Badge from '@/components/ui/Badge'
import Button from '@/components/ui/Button'
import BatteryBar from '@/components/ui/BatteryBar'
import Modal, { ModalHeader, ModalFooter } from '@/components/ui/Modal'
import DeviceFormModal from './DeviceFormModal'

export default function DeviceDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const deviceId = Number(id)

  const { data: device, isLoading } = useDeviceDetail(deviceId)
  const deleteMutation = useDeleteDevice()

  const [showEditModal, setShowEditModal] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  if (isLoading) return <p style={{ color: 'var(--text-sub)' }}>로딩 중...</p>
  if (!device) return <p style={{ color: 'var(--text-sub)' }}>장치를 찾을 수 없습니다.</p>

  const handleDelete = async () => {
    await deleteMutation.mutateAsync(deviceId)
    navigate('/devices')
  }

  const ca = device.currentAssignment

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>⌚</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>장치 상세 정보</h1>
      </div>

      <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap' }}>
        {/* 기본 정보 */}
        <div style={{ flex: 1, minWidth: 320, background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: 20 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14, paddingBottom: 8, borderBottom: '1px solid var(--gray-border)' }}>기본 정보</div>
          <div style={{ display: 'grid', gridTemplateColumns: '90px 1fr', gap: '10px 10px', fontSize: 12.5 }}>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>시리얼</span>
            <span style={{ fontWeight: 600 }}>{device.serialNumber}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>모델명</span>
            <span>{device.modelName || '—'}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>상태</span>
            <span>
              {ca
                ? <Badge variant="blue" dot>ASSIGNED</Badge>
                : <Badge variant={device.deviceStatus === 'ACTIVE' ? 'ok' : 'gray'} dot>{device.deviceStatus === 'ACTIVE' ? 'AVAILABLE' : device.deviceStatus}</Badge>
              }
            </span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>배터리</span>
            <span>{device.batteryLevel != null ? <BatteryBar level={device.batteryLevel} /> : '—'}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>최종 동기화</span>
            <span style={{ fontSize: 12, color: 'var(--text-sub)' }}>{device.lastSyncAt?.slice(0, 16).replace('T', ' ') ?? '—'}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>등록일</span>
            <span style={{ fontSize: 12, color: 'var(--text-sub)' }}>{device.createdAt?.slice(0, 10)}</span>
          </div>
          <div style={{ display: 'flex', gap: 6, marginTop: 16 }}>
            <Button variant="outline" onClick={() => setShowEditModal(true)}>수정</Button>
            <Button variant="danger" onClick={() => setShowDeleteConfirm(true)}>삭제</Button>
          </div>
        </div>

        {/* 현재 할당 환자 */}
        <div style={{ flex: 1, minWidth: 320, background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: 20 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14, paddingBottom: 8, borderBottom: '1px solid var(--gray-border)' }}>현재 할당 환자</div>
          {ca ? (
            <div style={{ display: 'grid', gridTemplateColumns: '90px 1fr', gap: '10px 10px', fontSize: 12.5 }}>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>환자코드</span>
              <span style={{ fontWeight: 600, color: 'var(--primary)' }}>{ca.patientCode}</span>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>환자명</span>
              <span>{ca.patientName}</span>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>시작일</span>
              <span>{ca.monitoringStartDate}</span>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>상태</span>
              <span><Badge variant="blue" dot>{ca.assignmentStatus}</Badge></span>
            </div>
          ) : (
            <p style={{ color: 'var(--text-sub)', fontSize: 12.5 }}>할당된 환자가 없습니다. (미할당)</p>
          )}
        </div>
      </div>

      {/* 할당 이력 */}
      {device.assignmentHistory.length > 0 && (
        <div className="table-panel">
          <div style={{ padding: '11px 16px', fontSize: 13, fontWeight: 600 }}>할당 이력</div>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th>환자명</th>
                <th>시작일</th>
                <th>종료일</th>
                <th>상태</th>
                <th>할당일시</th>
                <th>반납일시</th>
              </tr>
            </thead>
            <tbody>
              {device.assignmentHistory.map((h) => (
                <tr key={h.assignmentId}>
                  <td>{h.patientName}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.monitoringStartDate}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.monitoringEndDate ?? '—'}</td>
                  <td><Badge variant={h.assignmentStatus === 'ACTIVE' ? 'blue' : 'gray'} dot>{h.assignmentStatus}</Badge></td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.assignedAt?.slice(0, 16).replace('T', ' ')}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.returnedAt?.slice(0, 16).replace('T', ' ') ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* 수정 모달 */}
      <DeviceFormModal isOpen={showEditModal} onClose={() => setShowEditModal(false)} deviceId={deviceId} />

      {/* 삭제 확인 모달 */}
      <Modal isOpen={showDeleteConfirm} onClose={() => setShowDeleteConfirm(false)} width={360}>
        <ModalHeader title="장치 삭제 확인" onClose={() => setShowDeleteConfirm(false)} />
        <div style={{ padding: '22px 20px', textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
          <div style={{ fontSize: 36 }}>⚠️</div>
          <div style={{ fontSize: 14, fontWeight: 700 }}>장치를 삭제하시겠습니까?</div>
          <div style={{ fontSize: 12.5, color: 'var(--text-sub)', lineHeight: 1.6 }}>
            <strong>{device.serialNumber}</strong> 장치를 삭제합니다.
          </div>
        </div>
        <ModalFooter>
          <Button variant="outline" onClick={() => setShowDeleteConfirm(false)}>취소</Button>
          <Button variant="danger" onClick={handleDelete} loading={deleteMutation.isPending}>삭제 확인</Button>
        </ModalFooter>
      </Modal>
    </>
  )
}
