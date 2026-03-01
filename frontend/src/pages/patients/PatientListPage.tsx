import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { usePatientList } from '@/hooks/usePatients'
import Table from '@/components/ui/Table'
import type { Column } from '@/components/ui/Table'
import Badge from '@/components/ui/Badge'
import Button from '@/components/ui/Button'
import Pagination from '@/components/ui/Pagination'
import type { PatientListResponse, PatientStatus } from '@/types/patient'

const statusBadge: Record<string, { variant: 'ok' | 'gray' | 'blue'; label: string }> = {
  ACTIVE: { variant: 'ok', label: '활성' },
  INACTIVE: { variant: 'gray', label: '비활성' },
}

export default function PatientListPage() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<PatientStatus | ''>('')
  const [hasDevice, setHasDevice] = useState<'' | 'true' | 'false'>('')
  const [page, setPage] = useState(0)

  const { data, isLoading } = usePatientList({
    name: keyword || undefined,
    status: status || undefined,
    hasDevice: hasDevice === '' ? undefined : hasDevice === 'true',
    page,
    size: 20,
  })

  const columns: Column<PatientListResponse>[] = [
    { key: 'no', header: 'No.', width: 50, render: (_, i) => <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>{page * 20 + i + 1}</span> },
    {
      key: 'patientCode', header: '환자코드',
      render: (r) => <span style={{ fontWeight: 600, color: 'var(--primary)' }}>{r.patientCode}</span>,
    },
    { key: 'name', header: '환자명' },
    {
      key: 'gender', header: '성별', width: 60,
      render: (r) => r.gender === 'M' ? '남' : '여',
    },
    {
      key: 'status', header: '상태', width: 80,
      render: (r) => {
        const b = statusBadge[r.status]
        return b ? <Badge variant={b.variant} dot>{b.label}</Badge> : null
      },
    },
    {
      key: 'hasDevice', header: '장치할당', width: 80,
      render: (r) =>
        r.hasDevice
          ? <Badge variant="blue">할당</Badge>
          : <Badge variant="gray">미할당</Badge>,
    },
    {
      key: 'createdAt', header: '등록일', width: 110,
      render: (r) => <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>{r.createdAt?.slice(0, 10)}</span>,
    },
    {
      key: 'action', header: '액션', width: 60,
      render: (r) => (
        <Button
          variant="outline"
          size="sm"
          onClick={(e) => { e.stopPropagation(); navigate(`/patients/${r.id}`) }}
        >
          조회
        </Button>
      ),
    },
  ]

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>👤</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>환자 정보 관리</h1>
      </div>

      {/* 필터 바 */}
      <div style={{ background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
        <span style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-sub)' }}>☰ 필터</span>
        <input
          className="form-input"
          placeholder="🔍 환자명 또는 코드 검색"
          value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0) }}
          style={{ height: 30, width: 200 }}
        />
        <div style={{ width: 1, height: 18, background: 'var(--gray-border)' }} />
        <select
          className="form-select"
          value={status}
          onChange={(e) => { setStatus(e.target.value as PatientStatus | ''); setPage(0) }}
          style={{ height: 30 }}
        >
          <option value="">상태: 전체</option>
          <option value="ACTIVE">활성</option>
          <option value="INACTIVE">비활성</option>
        </select>
        <select
          className="form-select"
          value={hasDevice}
          onChange={(e) => { setHasDevice(e.target.value as '' | 'true' | 'false'); setPage(0) }}
          style={{ height: 30 }}
        >
          <option value="">장치할당: 전체</option>
          <option value="true">할당</option>
          <option value="false">미할당</option>
        </select>
      </div>

      {/* 테이블 */}
      <Table
        columns={columns}
        data={data?.content ?? []}
        rowKey={(r) => r.id}
        title="환자 목록"
        totalCount={data?.totalElements}
        warnRow={(r) => r.hasDevice}
        emptyText={isLoading ? '로딩 중...' : '등록된 환자가 없습니다.'}
        actions={<Button variant="primary" onClick={() => navigate('/patients/new')}>+ 신규 등록</Button>}
        footer={
          data && (
            <Pagination
              currentPage={data.number + 1}
              totalPages={data.totalPages}
              onPageChange={(p) => setPage(p - 1)}
            />
          )
        }
      />
    </>
  )
}
