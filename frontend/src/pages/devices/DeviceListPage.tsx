import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDeviceList } from '@/hooks/useDevices'
import Table from '@/components/ui/Table'
import type { Column } from '@/components/ui/Table'
import Badge from '@/components/ui/Badge'
import Button from '@/components/ui/Button'
import BatteryBar from '@/components/ui/BatteryBar'
import Pagination from '@/components/ui/Pagination'
import DeviceFormModal from './DeviceFormModal'
import type { DeviceListResponse, DeviceStatus } from '@/types/device'

const statusBadge: Record<string, { variant: 'ok' | 'blue' | 'warn' | 'gray' | 'danger'; label: string }> = {
  ACTIVE: { variant: 'ok', label: 'AVAILABLE' },
  RETIRED: { variant: 'gray', label: 'RETIRED' },
}

function isOver24h(dateStr: string | null): boolean {
  if (!dateStr) return false
  return Date.now() - new Date(dateStr).getTime() > 24 * 60 * 60 * 1000
}

export default function DeviceListPage() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [deviceStatus, setDeviceStatus] = useState<DeviceStatus | ''>('')
  const [page, setPage] = useState(0)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const { data, isLoading } = useDeviceList({
    keyword: keyword || undefined,
    deviceStatus: deviceStatus || undefined,
    page,
    size: 20,
  })

  const columns: Column<DeviceListResponse>[] = [
    { key: 'no', header: 'No.', width: 50, render: (_, i) => <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>{page * 20 + i + 1}</span> },
    {
      key: 'serialNumber', header: '시리얼 번호',
      render: (r) => <span style={{ fontWeight: 600 }}>{r.serialNumber}</span>,
    },
    { key: 'modelName', header: '모델명', render: (r) => <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>{r.modelName || '—'}</span> },
    {
      key: 'deviceStatus', header: '상태', width: 100,
      render: (r) => {
        if (r.assignedPatientName) return <Badge variant="blue" dot>ASSIGNED</Badge>
        const b = statusBadge[r.deviceStatus]
        return b ? <Badge variant={b.variant} dot>{b.label}</Badge> : null
      },
    },
    {
      key: 'assignedPatientName', header: '할당 환자', width: 100,
      render: (r) => r.assignedPatientName ?? <span style={{ color: 'var(--text-sub)' }}>—</span>,
    },
    {
      key: 'batteryLevel', header: '배터리', width: 100,
      render: (r) => r.batteryLevel != null ? <BatteryBar level={r.batteryLevel} /> : <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>—</span>,
    },
    {
      key: 'lastSyncAt', header: '최종 동기화', width: 140,
      render: (r) => {
        if (!r.lastSyncAt) return <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>—</span>
        const over = isOver24h(r.lastSyncAt)
        return (
          <span style={{ color: over ? 'var(--warn)' : 'var(--text-sub)', fontWeight: over ? 500 : 400, fontSize: 12 }}>
            {r.lastSyncAt.slice(0, 16).replace('T', ' ')}
          </span>
        )
      },
    },
    {
      key: 'action', header: '액션', width: 60,
      render: (r) => (
        <Button variant="outline" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/devices/${r.id}`) }}>
          조회
        </Button>
      ),
    },
  ]

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>⌚</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>장치 정보 관리</h1>
      </div>

      {/* 필터 */}
      <div style={{ background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
        <span style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-sub)' }}>☰ 필터</span>
        <input
          className="form-input"
          placeholder="🔍 시리얼 번호 검색"
          value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0) }}
          style={{ height: 30, width: 200 }}
        />
        <div style={{ width: 1, height: 18, background: 'var(--gray-border)' }} />
        <select
          className="form-select"
          value={deviceStatus}
          onChange={(e) => { setDeviceStatus(e.target.value as DeviceStatus | ''); setPage(0) }}
          style={{ height: 30 }}
        >
          <option value="">상태: 전체</option>
          <option value="ACTIVE">AVAILABLE</option>
          <option value="RETIRED">RETIRED</option>
        </select>
      </div>

      {/* 테이블 */}
      <Table
        columns={columns}
        data={data?.content ?? []}
        rowKey={(r) => r.id}
        title="장치 목록"
        totalCount={data?.totalElements}
        warnRow={(r) => (r.batteryLevel != null && r.batteryLevel <= 20)}
        emptyText={isLoading ? '로딩 중...' : '등록된 장치가 없습니다.'}
        actions={<Button variant="primary" onClick={() => setShowCreateModal(true)}>+ 신규 등록</Button>}
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

      <DeviceFormModal isOpen={showCreateModal} onClose={() => setShowCreateModal(false)} />
    </>
  )
}
