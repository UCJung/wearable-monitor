import { useState, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { differenceInDays, format, subDays } from 'date-fns'
import { usePatientList } from '@/hooks/usePatients'
import { useBiometricHistory } from '@/hooks/useBiometricHistory'
import { exportApi } from '@/api/exportApi'
import { useToastStore } from '@/stores/toastStore'
import Button from '@/components/ui/Button'
import Pagination from '@/components/ui/Pagination'

const ITEM_CODES = [
  { code: 'HEART_RATE', label: '심박수', category: 'VITAL_SIGN' },
  { code: 'BLOOD_OXYGEN', label: 'SpO2', category: 'VITAL_SIGN' },
  { code: 'SKIN_TEMP', label: '피부온도', category: 'VITAL_SIGN' },
  { code: 'BLOOD_PRESSURE', label: '혈압', category: 'VITAL_SIGN' },
  { code: 'STRESS', label: '스트레스', category: 'VITAL_SIGN' },
  { code: 'STEPS', label: '걸음수', category: 'ACTIVITY' },
  { code: 'CALORIES', label: '칼로리', category: 'ACTIVITY' },
  { code: 'DISTANCE', label: '이동거리', category: 'ACTIVITY' },
  { code: 'SLEEP_DURATION', label: '수면시간', category: 'SLEEP' },
  { code: 'SLEEP_STAGE', label: '수면단계', category: 'SLEEP' },
  { code: 'SLEEP_SCORE', label: '수면점수', category: 'SLEEP' },
  { code: 'ENERGY_SCORE', label: '에너지점수', category: 'AI_SCORE' },
]

const CATEGORY_BG: Record<string, string> = {
  VITAL_SIGN: 'var(--cat-vital-bg)',
  ACTIVITY: 'var(--cat-activity-bg)',
  SLEEP: 'var(--cat-sleep-bg)',
  AI_SCORE: 'var(--cat-ai-bg)',
}

const PAGE_SIZE = 50

export default function BiometricHistoryPage() {
  const toast = useToastStore()
  const [searchParams] = useSearchParams()

  const today = format(new Date(), 'yyyy-MM-dd')
  const weekAgo = format(subDays(new Date(), 6), 'yyyy-MM-dd')

  // URL 파라미터에서 초기값
  const initialPatientId = searchParams.get('patientId') ? Number(searchParams.get('patientId')) : 0
  const initialStart = searchParams.get('start') ?? weekAgo
  const initialEnd = searchParams.get('end') ?? today

  const [selectedPatientId, setSelectedPatientId] = useState(initialPatientId)
  const [startDate, setStartDate] = useState(initialStart)
  const [endDate, setEndDate] = useState(initialEnd)
  const [selectedItems, setSelectedItems] = useState<string[]>([])
  const [dateError, setDateError] = useState('')
  const [queryEnabled, setQueryEnabled] = useState(initialPatientId > 0)
  const [exporting, setExporting] = useState(false)
  const [page, setPage] = useState(0)

  // 환자 목록
  const { data: patientPage } = usePatientList({ size: 100, status: 'ACTIVE' })
  const patients = patientPage?.content ?? []

  // 선택된 환자 정보
  const selectedPatient = useMemo(
    () => patients.find((p) => p.id === selectedPatientId),
    [patients, selectedPatientId],
  )

  // 수집 이력 조회
  const condition = useMemo(() => ({
    itemCodes: selectedItems.length > 0 ? selectedItems : undefined,
    start: startDate,
    end: endDate,
    page,
    size: PAGE_SIZE,
  }), [startDate, endDate, selectedItems, page])

  const { data: historyData, isLoading, isFetching } = useBiometricHistory(
    queryEnabled ? selectedPatientId : 0,
    condition,
  )

  const records = historyData?.records ?? []
  const totalCount = historyData?.totalCount ?? 0
  const hasMore = historyData?.hasMore ?? false
  const totalPages = Math.max(1, Math.ceil(totalCount / PAGE_SIZE))

  const handleSearch = () => {
    if (selectedPatientId === 0) return

    const days = differenceInDays(new Date(endDate), new Date(startDate))
    if (days < 0) {
      setDateError('시작일이 종료일보다 이후입니다.')
      return
    }
    if (days > 90) {
      setDateError('조회 기간은 최대 90일까지 가능합니다.')
      return
    }
    setDateError('')
    setPage(0)
    setQueryEnabled(true)
  }

  const handleExport = async () => {
    if (selectedPatientId === 0) return
    setExporting(true)
    toast.push({ type: 'info', title: '엑셀 다운로드 준비 중', message: '데이터를 처리하고 있습니다...' })
    try {
      const res = await exportApi.downloadPatientExcel(selectedPatientId, { start: startDate, end: endDate })
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const a = document.createElement('a')
      a.href = url
      const code = selectedPatient?.patientCode ?? `PT-${selectedPatientId}`
      a.download = `${code}_수집이력_${format(new Date(), 'yyyyMMdd')}.xlsx`
      a.click()
      window.URL.revokeObjectURL(url)
      toast.push({ type: 'success', title: '다운로드 완료' })
    } catch {
      toast.push({ type: 'danger', title: '다운로드 실패', message: '엑셀 다운로드 중 오류가 발생했습니다.' })
    } finally {
      setExporting(false)
    }
  }

  const handleItemToggle = (code: string) => {
    setSelectedItems((prev) =>
      prev.includes(code) ? prev.filter((x) => x !== code) : [...prev, code],
    )
    setQueryEnabled(false)
  }

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>📊</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>수집 데이터 이력</h1>
      </div>

      {/* 조회 조건 패널 */}
      <div style={{ background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: '14px 16px' }}>
        <div style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 12, paddingBottom: 8, borderBottom: '1px solid var(--gray-border)' }}>☰ 조회 조건</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 12, alignItems: 'end' }}>
          {/* 환자 선택 */}
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 5 }}>
              환자 선택 <span style={{ color: 'var(--danger)' }}>*</span>
            </div>
            <select
              className="form-select"
              value={selectedPatientId}
              onChange={(e) => { setSelectedPatientId(Number(e.target.value)); setQueryEnabled(false) }}
              style={{ width: '100%' }}
            >
              <option value={0}>환자를 선택하세요</option>
              {patients.map((p) => (
                <option key={p.id} value={p.id}>{p.patientCode} {p.name}</option>
              ))}
            </select>
          </div>

          {/* 조회 기간 */}
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 5 }}>조회 기간</div>
            <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <input type="date" className="form-input" value={startDate} onChange={(e) => { setStartDate(e.target.value); setQueryEnabled(false) }} style={{ width: 130 }} />
              <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>~</span>
              <input type="date" className="form-input" value={endDate} onChange={(e) => { setEndDate(e.target.value); setQueryEnabled(false) }} style={{ width: 130 }} />
            </div>
            {dateError && <p style={{ fontSize: 11, color: 'var(--danger)', marginTop: 4 }}>{dateError}</p>}
          </div>

          {/* 항목 선택 */}
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 5 }}>수집 항목</div>
            <div style={{ position: 'relative' }}>
              <details style={{ position: 'relative' }}>
                <summary className="form-input" style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', listStyle: 'none' }}>
                  {selectedItems.length === 0 ? '전체 항목' : `${selectedItems.length}개 선택`}
                </summary>
                <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 5, maxHeight: 200, overflowY: 'auto', zIndex: 10, padding: 4 }}>
                  {ITEM_CODES.map((ic) => (
                    <label key={ic.code} style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '4px 8px', fontSize: 12, cursor: 'pointer' }}>
                      <input type="checkbox" checked={selectedItems.includes(ic.code)} onChange={() => handleItemToggle(ic.code)} />
                      {ic.label}
                    </label>
                  ))}
                </div>
              </details>
            </div>
          </div>

          {/* 조회 버튼 */}
          <div>
            <Button variant="primary" onClick={handleSearch} loading={isFetching} disabled={selectedPatientId === 0}>
              조회
            </Button>
          </div>
        </div>
      </div>

      {/* 5,000건 초과 배너 */}
      {queryEnabled && totalCount > 5000 && (
        <div style={{ background: '#FFF8E1', border: '1px solid #FFE082', borderRadius: 8, padding: '10px 14px', display: 'flex', alignItems: 'center', gap: 8, fontSize: 12.5 }}>
          <span style={{ fontSize: 16 }}>⚠️</span>
          <span>
            조회 결과가 <strong style={{ color: 'var(--warn)' }}>{totalCount.toLocaleString()}건</strong>입니다.
            기간을 줄이거나 항목을 선택하여 범위를 좁혀주세요.
          </span>
        </div>
      )}

      {/* 이력 테이블 */}
      {queryEnabled && selectedPatientId > 0 && (
        <div className="table-panel">
          <div style={{ padding: '11px 16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--gray-border)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <span style={{ fontSize: 13, fontWeight: 600 }}>수집 이력</span>
              <span style={{ fontSize: 11.5, color: 'var(--text-sub)' }}>
                총 <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{totalCount.toLocaleString()}</span>건
                {hasMore && ' (더 있음)'}
              </span>
            </div>
            <Button variant="outline" size="sm" onClick={handleExport} loading={exporting} disabled={exporting}>
              📥 엑셀 다운로드
            </Button>
          </div>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: 700 }}>
              <thead>
                <tr>
                  <th>No.</th>
                  <th>수집 항목</th>
                  <th>분류</th>
                  <th>측정 일시</th>
                  <th style={{ textAlign: 'right' }}>수치값</th>
                  <th>단위</th>
                  <th>텍스트값</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>로딩 중...</td></tr>
                ) : records.length === 0 ? (
                  <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>데이터가 없습니다.</td></tr>
                ) : (
                  records.map((rec, i) => {
                    const bg = CATEGORY_BG[rec.category] ?? undefined
                    return (
                      <tr key={rec.id} style={bg ? { background: bg } : undefined}>
                        <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{page * PAGE_SIZE + i + 1}</td>
                        <td style={{ fontWeight: 600 }}>{rec.itemNameKo}</td>
                        <td style={{ fontSize: 12, color: 'var(--text-sub)' }}>{rec.category}</td>
                        <td style={{ fontSize: 12 }}>{rec.measuredAt?.slice(0, 19).replace('T', ' ')}</td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>
                          {rec.valueNumeric != null ? rec.valueNumeric.toLocaleString() : <span style={{ color: 'var(--text-sub)' }}>—</span>}
                        </td>
                        <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{rec.unit ?? '—'}</td>
                        <td style={{ fontSize: 12, maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={rec.valueText ?? undefined}>
                          {rec.valueText ?? '—'}
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>

          <Pagination currentPage={page + 1} totalPages={totalPages} onPageChange={(p) => setPage(p - 1)} />
        </div>
      )}
    </>
  )
}
