import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { usePatientDetail, useDeletePatient } from '@/hooks/usePatients'
import Badge from '@/components/ui/Badge'
import Button from '@/components/ui/Button'
import BatteryBar from '@/components/ui/BatteryBar'
import Modal, { ModalHeader, ModalFooter } from '@/components/ui/Modal'
import AssignDeviceModal from './AssignDeviceModal'
import ReturnDeviceModal from './ReturnDeviceModal'

export default function PatientDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const patientId = Number(id)

  const { data: patient, isLoading } = usePatientDetail(patientId)
  const deleteMutation = useDeletePatient()

  const [showAssign, setShowAssign] = useState(false)
  const [showReturn, setShowReturn] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  if (isLoading) return <p style={{ color: 'var(--text-sub)' }}>로딩 중...</p>
  if (!patient) return <p style={{ color: 'var(--text-sub)' }}>환자를 찾을 수 없습니다.</p>

  const handleDelete = async () => {
    await deleteMutation.mutateAsync(patientId)
    navigate('/patients')
  }

  const cd = patient.currentDevice
  const patientLabel = `${patient.patientCode}  ${patient.name}`

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>👤</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>환자 상세 정보</h1>
      </div>

      <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap' }}>
        {/* 기본 정보 카드 */}
        <div style={{ flex: 1, minWidth: 320, background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: 20 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14, paddingBottom: 8, borderBottom: '1px solid var(--gray-border)' }}>기본 정보</div>
          <div style={{ display: 'grid', gridTemplateColumns: '90px 1fr', gap: '10px 10px', fontSize: 12.5 }}>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>환자코드</span>
            <span style={{ fontWeight: 600, color: 'var(--primary)' }}>{patient.patientCode}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>환자명</span>
            <span>{patient.name}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>생년월일</span>
            <span>{patient.birthDate}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>성별</span>
            <span>{patient.gender === 'M' ? '남' : '여'}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>상태</span>
            <span><Badge variant={patient.status === 'ACTIVE' ? 'ok' : 'gray'} dot>{patient.status === 'ACTIVE' ? '활성' : '비활성'}</Badge></span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>등록일</span>
            <span>{patient.createdAt?.slice(0, 10)}</span>
            <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>특이사항</span>
            <span>{patient.notes || '—'}</span>
          </div>
          <div style={{ display: 'flex', gap: 6, marginTop: 16 }}>
            <Button variant="outline" onClick={() => navigate(`/patients/${patientId}/edit`)}>수정</Button>
            <Button variant="danger" onClick={() => setShowDeleteConfirm(true)}>삭제</Button>
          </div>
        </div>

        {/* 현재 할당 장치 카드 */}
        <div style={{ flex: 1, minWidth: 320, background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: 20 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14, paddingBottom: 8, borderBottom: '1px solid var(--gray-border)' }}>현재 할당 장치</div>
          {cd ? (
            <div style={{ display: 'grid', gridTemplateColumns: '90px 1fr', gap: '10px 10px', fontSize: 12.5 }}>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>시리얼</span>
              <span>{cd.serialNumber}</span>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>모델명</span>
              <span>{cd.modelName}</span>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>시작일</span>
              <span>{cd.monitoringStartDate}</span>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>상태</span>
              <span><Badge variant="blue" dot>{cd.assignmentStatus}</Badge></span>
              <span style={{ color: 'var(--text-sub)', fontWeight: 600 }}>배터리</span>
              <span><BatteryBar level={50} /></span>
            </div>
          ) : (
            <p style={{ color: 'var(--text-sub)', fontSize: 12.5, marginBottom: 12 }}>할당된 장치가 없습니다.</p>
          )}
          <div style={{ marginTop: 14 }}>
            {cd ? (
              <Button variant="danger" onClick={() => setShowReturn(true)}>🔓 장치 해제</Button>
            ) : (
              <Button variant="primary" onClick={() => setShowAssign(true)}>장치 할당</Button>
            )}
          </div>
        </div>
      </div>

      {/* 할당 이력 테이블 */}
      {patient.assignmentHistory.length > 0 && (
        <div className="table-panel">
          <div style={{ padding: '11px 16px', fontSize: 13, fontWeight: 600 }}>할당 이력 (최근 {patient.assignmentHistory.length}건)</div>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th>시리얼</th>
                <th>시작일</th>
                <th>종료일</th>
                <th>상태</th>
                <th>할당일시</th>
                <th>반납일시</th>
              </tr>
            </thead>
            <tbody>
              {patient.assignmentHistory.map((h) => (
                <tr key={h.assignmentId}>
                  <td>{h.serialNumber}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.monitoringStartDate}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.monitoringEndDate ?? '—'}</td>
                  <td>
                    <Badge variant={h.assignmentStatus === 'ACTIVE' ? 'blue' : 'gray'} dot>
                      {h.assignmentStatus}
                    </Badge>
                  </td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.assignedAt?.slice(0, 16).replace('T', ' ')}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{h.returnedAt?.slice(0, 16).replace('T', ' ') ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* 모달들 */}
      <AssignDeviceModal
        isOpen={showAssign}
        onClose={() => setShowAssign(false)}
        patientId={patientId}
        patientLabel={patientLabel}
      />

      {cd && (
        <ReturnDeviceModal
          isOpen={showReturn}
          onClose={() => setShowReturn(false)}
          assignmentId={cd.assignmentId}
          patientLabel={patientLabel}
          serialNumber={cd.serialNumber}
        />
      )}

      {/* 삭제 확인 모달 */}
      <Modal isOpen={showDeleteConfirm} onClose={() => setShowDeleteConfirm(false)} width={360}>
        <ModalHeader title="환자 삭제 확인" onClose={() => setShowDeleteConfirm(false)} />
        <div style={{ padding: '22px 20px', textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
          <div style={{ fontSize: 36 }}>⚠️</div>
          <div style={{ fontSize: 14, fontWeight: 700 }}>환자를 삭제하시겠습니까?</div>
          <div style={{ fontSize: 12.5, color: 'var(--text-sub)', lineHeight: 1.6 }}>
            <strong>{patientLabel}</strong> 환자를 삭제합니다.<br />
            장치가 할당 중인 경우 삭제할 수 없습니다.
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
