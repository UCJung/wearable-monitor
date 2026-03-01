import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { assignmentApi } from '@/api/assignmentApi'
import Badge from '@/components/ui/Badge'
import Pagination from '@/components/ui/Pagination'
import type { AssignmentStatus, AssignmentSearchCondition } from '@/types/assignment'

const PAGE_SIZE = 20

export default function AssignmentHistoryPage() {
  const navigate = useNavigate()

  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState<AssignmentStatus | ''>('')
  const [page, setPage] = useState(0)

  const condition: AssignmentSearchCondition = {
    keyword: keyword || undefined,
    status: statusFilter || undefined,
    page,
    size: PAGE_SIZE,
  }

  const { data, isLoading } = useQuery({
    queryKey: ['assignments', condition],
    queryFn: () => assignmentApi.getAssignments(condition).then((r) => r.data.data!),
  })

  const rows = data?.content ?? []
  const totalPages = data?.totalPages ?? 1

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>📋</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>할당/해제 이력</h1>
      </div>

      {/* 필터 바 */}
      <div style={{ background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
        <span style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-sub)' }}>☰ 필터</span>
        <select
          className="form-select"
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value as AssignmentStatus | ''); setPage(0) }}
          style={{ height: 30 }}
        >
          <option value="">상태 : 전체</option>
          <option value="ACTIVE">ACTIVE (할당 중)</option>
          <option value="RETURNED">RETURNED (반납)</option>
        </select>
        <div style={{ width: 1, height: 18, background: 'var(--gray-border)' }} />
        <input
          className="form-input"
          placeholder="환자명/코드, 시리얼 검색"
          value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0) }}
          style={{ height: 30, width: 220 }}
        />
      </div>

      {/* 테이블 */}
      <div className="table-panel">
        <div style={{ padding: '11px 16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--gray-border)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span style={{ fontSize: 13, fontWeight: 600 }}>이력 목록</span>
            <span style={{ fontSize: 11.5, color: 'var(--text-sub)' }}>
              총 <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{data?.totalElements ?? 0}</span>건
            </span>
          </div>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th>No.</th>
              <th>환자 코드</th>
              <th>환자명</th>
              <th>장치 시리얼</th>
              <th>모델명</th>
              <th style={{ textAlign: 'center' }}>상태</th>
              <th>모니터링 시작</th>
              <th>모니터링 종료</th>
              <th>할당일시</th>
              <th>반납일시</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={10} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>로딩 중...</td></tr>
            ) : rows.length === 0 ? (
              <tr><td colSpan={10} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>데이터가 없습니다.</td></tr>
            ) : (
              rows.map((row, i) => (
                <tr
                  key={row.id}
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/patients/${row.patientId}`)}
                >
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{page * PAGE_SIZE + i + 1}</td>
                  <td style={{ fontWeight: 600, color: 'var(--primary)' }}>{row.patientCode}</td>
                  <td>{row.patientName}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{row.serialNumber}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{row.modelName}</td>
                  <td style={{ textAlign: 'center' }}>
                    {row.assignmentStatus === 'ACTIVE' && <Badge variant="blue">할당 중</Badge>}
                    {row.assignmentStatus === 'RETURNED' && <Badge variant="gray">반납</Badge>}
                  </td>
                  <td style={{ fontSize: 12 }}>{row.monitoringStartDate}</td>
                  <td style={{ fontSize: 12 }}>
                    {row.monitoringEndDate ?? <span style={{ color: 'var(--ok)', fontWeight: 500 }}>진행 중</span>}
                  </td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{row.assignedAt?.slice(0, 16).replace('T', ' ')}</td>
                  <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>
                    {row.returnedAt ? row.returnedAt.slice(0, 16).replace('T', ' ') : '—'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        <Pagination currentPage={page + 1} totalPages={totalPages} onPageChange={(p) => setPage(p - 1)} />
      </div>
    </>
  )
}
