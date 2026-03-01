import { useState, useMemo, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAssignmentStatus, useCountdown } from '@/hooks/useMonitoring'
import SummaryCard from '@/components/ui/SummaryCard'
import Badge from '@/components/ui/Badge'
import BatteryBar from '@/components/ui/BatteryBar'
import Button from '@/components/ui/Button'
import Pagination from '@/components/ui/Pagination'
import type { AssignmentStatusItem } from '@/types/biometric'

type CollectionFilter = 'ALL' | 'OK' | 'WARN' | 'NONE'

function getCollectionStatus(item: AssignmentStatusItem): CollectionFilter {
  if (!item.deviceId) return 'NONE'
  return item.recentlyCollected ? 'OK' : 'WARN'
}

const PAGE_SIZE = 20

export default function AssignmentStatusPage() {
  const navigate = useNavigate()
  const { data, dataUpdatedAt, isLoading } = useAssignmentStatus()
  const { display, reset } = useCountdown(300)

  // 카운트다운 리셋: 데이터 갱신 시
  useEffect(() => {
    if (dataUpdatedAt) reset()
  }, [dataUpdatedAt, reset])

  const [filter, setFilter] = useState<CollectionFilter>('ALL')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(0)

  const patients = data?.patients ?? []
  const summary = data?.summary

  // 필터링 + 정렬 (미수집 상단)
  const filtered = useMemo(() => {
    let list = patients

    if (filter !== 'ALL') {
      list = list.filter((p) => getCollectionStatus(p) === filter)
    }

    if (keyword) {
      const kw = keyword.toLowerCase()
      list = list.filter(
        (p) => p.patientName.toLowerCase().includes(kw) || p.patientCode.toLowerCase().includes(kw)
      )
    }

    // 미수집 상단 정렬
    return [...list].sort((a, b) => {
      const sa = getCollectionStatus(a)
      const sb = getCollectionStatus(b)
      if (sa === 'WARN' && sb !== 'WARN') return -1
      if (sa !== 'WARN' && sb === 'WARN') return 1
      return 0
    })
  }, [patients, filter, keyword])

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const paged = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>📡</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>장치 할당 현황</h1>
      </div>

      {/* 요약 카드 4개 */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
        <SummaryCard icon="👥" iconBg="#eef2ff" label="전체 환자" value={summary?.total ?? 0} sub="등록 환자 전체" />
        <SummaryCard icon="⌚" iconBg="#e0f0ff" label="장치 할당" value={summary?.assigned ?? 0} sub={`미할당 ${(summary?.total ?? 0) - (summary?.assigned ?? 0)}명`} />
        <SummaryCard icon="✅" iconBg="var(--ok-bg)" label="정상 수집" value={summary?.collecting ?? 0} sub="24시간 이내" />
        <SummaryCard icon="⚠️" iconBg="var(--warn-bg)" label="미수집 (이상)" value={summary?.notCollecting ?? 0} sub="24시간 초과" />
      </div>

      {/* 필터 바 */}
      <div style={{ background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
        <span style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-sub)' }}>☰ 필터</span>
        <select
          className="form-select"
          value={filter}
          onChange={(e) => { setFilter(e.target.value as CollectionFilter); setPage(0) }}
          style={{ height: 30 }}
        >
          <option value="ALL">수집 상태 : 전체</option>
          <option value="OK">✅ 정상 (24h 이내)</option>
          <option value="WARN">⚠️ 미수집 (24h 초과)</option>
          <option value="NONE">— 미할당</option>
        </select>
        <div style={{ width: 1, height: 18, background: 'var(--gray-border)' }} />
        <input
          className="form-input"
          placeholder="🔍 환자명 또는 코드 검색"
          value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0) }}
          style={{ height: 30, width: 200 }}
        />
        <div style={{ marginLeft: 'auto', fontSize: 11.5, color: 'var(--text-sub)', display: 'flex', alignItems: 'center', gap: 5 }}>
          <span className="refresh-dot" />
          다음 갱신 <strong>{display}</strong>
        </div>
      </div>

      {/* 테이블 */}
      <div className="table-panel">
        <div style={{ padding: '11px 16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--gray-border)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span style={{ fontSize: 13, fontWeight: 600 }}>전체 환자 현황</span>
            <span style={{ fontSize: 11.5, color: 'var(--text-sub)' }}>
              총 <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{filtered.length}</span>명
            </span>
          </div>
          <Button variant="outline" size="sm" onClick={() => navigate('/history/biometric')}>
            📥 엑셀 다운로드
          </Button>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th>No.</th>
              <th>환자 코드</th>
              <th>환자명</th>
              <th>장치 시리얼</th>
              <th>모델명</th>
              <th>할당일</th>
              <th style={{ textAlign: 'center' }}>배터리</th>
              <th>최종 수집 일시</th>
              <th style={{ textAlign: 'center' }}>수집 상태</th>
              <th style={{ textAlign: 'center' }}>액션</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={10} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>로딩 중...</td></tr>
            ) : paged.length === 0 ? (
              <tr><td colSpan={10} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>데이터가 없습니다.</td></tr>
            ) : (
              paged.map((p, i) => {
                const cs = getCollectionStatus(p)
                const isWarn = cs === 'WARN'
                const isNone = cs === 'NONE'
                return (
                  <tr
                    key={p.patientId}
                    className={isWarn ? 'warn-row' : undefined}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/history/biometric?patientId=${p.patientId}`)}
                  >
                    <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{page * PAGE_SIZE + i + 1}</td>
                    <td style={{ fontWeight: 600, color: isNone ? 'var(--text-sub)' : 'var(--primary)' }}>{p.patientCode}</td>
                    <td style={{ color: isNone ? 'var(--text-sub)' : undefined }}>{p.patientName}</td>
                    <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{p.serialNumber ?? '—'}</td>
                    <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{p.modelName ?? '—'}</td>
                    <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>—</td>
                    <td style={{ textAlign: 'center' }}>
                      {p.batteryLevel != null ? <BatteryBar level={p.batteryLevel} /> : <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>—</span>}
                    </td>
                    <td style={isWarn ? { color: 'var(--warn)', fontWeight: 500 } : undefined}>
                      {p.lastCollectedAt ? p.lastCollectedAt.slice(0, 16).replace('T', ' ') : '—'}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      {cs === 'OK' && <Badge variant="ok" dot>✅ 정상</Badge>}
                      {cs === 'WARN' && <Badge variant="warn" dot>⚠ 미수집</Badge>}
                      {cs === 'NONE' && <Badge variant="gray">— 미할당</Badge>}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      {isNone ? (
                        <Button variant="primary" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/patients/${p.patientId}`) }}>할당</Button>
                      ) : (
                        <Button variant="outline" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/patients/${p.patientId}`) }}>조회</Button>
                      )}
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>

        <Pagination currentPage={page + 1} totalPages={totalPages} onPageChange={(p) => setPage(p - 1)} />
      </div>
    </>
  )
}
