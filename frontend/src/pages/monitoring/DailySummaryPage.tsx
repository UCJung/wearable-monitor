import { useState, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { differenceInDays, format, subDays } from 'date-fns'
import { useDailySummary } from '@/hooks/useDailySummary'
import { usePatientList } from '@/hooks/usePatients'
import { exportApi } from '@/api/exportApi'
import { useToastStore } from '@/stores/toastStore'
import Button from '@/components/ui/Button'
import Pagination from '@/components/ui/Pagination'

const ITEM_CODES = [
  { code: 'HEART_RATE', label: '심박수' },
  { code: 'BLOOD_OXYGEN', label: 'SpO2' },
  { code: 'SKIN_TEMP', label: '피부온도' },
  { code: 'BLOOD_PRESSURE', label: '혈압' },
  { code: 'STRESS', label: '스트레스' },
  { code: 'STEPS', label: '걸음수' },
  { code: 'CALORIES', label: '칼로리' },
  { code: 'DISTANCE', label: '이동거리' },
  { code: 'SLEEP_DURATION', label: '수면시간' },
  { code: 'SLEEP_STAGE', label: '수면단계' },
  { code: 'SLEEP_SCORE', label: '수면점수' },
  { code: 'ENERGY_SCORE', label: '에너지점수' },
]

const PAGE_SIZE = 20

export default function DailySummaryPage() {
  const navigate = useNavigate()
  const toast = useToastStore()
  const today = format(new Date(), 'yyyy-MM-dd')
  const weekAgo = format(subDays(new Date(), 6), 'yyyy-MM-dd')

  const [startDate, setStartDate] = useState(weekAgo)
  const [endDate, setEndDate] = useState(today)
  const [selectedPatientIds, setSelectedPatientIds] = useState<number[]>([])
  const [selectedItems, setSelectedItems] = useState<string[]>([])
  const [dateError, setDateError] = useState('')
  const [queryEnabled, setQueryEnabled] = useState(false)
  const [exporting, setExporting] = useState(false)
  const [page, setPage] = useState(0)

  // 환자 목록 (전체)
  const { data: patientPage } = usePatientList({ size: 100, status: 'ACTIVE' })
  const patients = patientPage?.content ?? []

  // 집계 데이터
  const condition = useMemo(() => ({
    patientIds: selectedPatientIds.length > 0 ? selectedPatientIds : undefined,
    itemCodes: selectedItems.length > 0 ? selectedItems : undefined,
    start: startDate,
    end: endDate,
  }), [startDate, endDate, selectedPatientIds, selectedItems])

  const { data: summaryData, isLoading, isFetching } = useDailySummary(condition, queryEnabled)

  // 표시할 항목 코드
  const displayItems = selectedItems.length > 0
    ? ITEM_CODES.filter((ic) => selectedItems.includes(ic.code))
    : ITEM_CODES

  // 집계 데이터 → 테이블 행 변환
  const rows = useMemo(() => {
    if (!summaryData?.items) return []

    const grouped = new Map<string, Map<string, { avg: number | null; count: number }>>()

    for (const item of summaryData.items) {
      const key = `${item.patientId}_${item.measureDate}`
      if (!grouped.has(key)) grouped.set(key, new Map())
      grouped.get(key)!.set(item.itemCode, { avg: item.avgValue, count: item.measureCount })
    }

    const result: { patientId: number; patientCode: string; patientName: string; date: string; items: Map<string, { avg: number | null; count: number }> }[] = []

    for (const [key, items] of grouped) {
      const [pidStr, date] = key.split('_')
      const pid = Number(pidStr)
      const pt = patients.find((p) => p.id === pid)
      result.push({
        patientId: pid,
        patientCode: pt?.patientCode ?? `PT-${pid}`,
        patientName: pt?.name ?? '—',
        date,
        items,
      })
    }

    return result.sort((a, b) => b.date.localeCompare(a.date) || a.patientCode.localeCompare(b.patientCode))
  }, [summaryData, patients])

  // 합계
  const totals = useMemo(() => {
    const t = new Map<string, { sum: number; count: number }>()
    for (const row of rows) {
      for (const [code, val] of row.items) {
        if (!t.has(code)) t.set(code, { sum: 0, count: 0 })
        const entry = t.get(code)!
        entry.count += val.count
        if (val.avg != null) entry.sum += val.avg * val.count
      }
    }
    return t
  }, [rows])

  const totalPages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE))
  const pagedRows = rows.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)

  const handleSearch = () => {
    const days = differenceInDays(new Date(endDate), new Date(startDate))
    if (days < 0) {
      setDateError('시작일이 종료일보다 이후입니다.')
      return
    }
    if (days > 31) {
      setDateError('조회 기간은 최대 31일까지 가능합니다.')
      return
    }
    setDateError('')
    setPage(0)
    setQueryEnabled(true)
  }

  const handleExport = async () => {
    setExporting(true)
    toast.push({ type: 'info', title: '엑셀 다운로드 준비 중', message: '데이터를 처리하고 있습니다...' })
    try {
      const res = await exportApi.downloadAllExcel({ start: startDate, end: endDate })
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const a = document.createElement('a')
      a.href = url
      a.download = `전체_수집이력_${format(new Date(), 'yyyyMMdd')}.xlsx`
      a.click()
      window.URL.revokeObjectURL(url)
      toast.push({ type: 'success', title: '다운로드 완료' })
    } catch {
      toast.push({ type: 'danger', title: '다운로드 실패', message: '엑셀 다운로드 중 오류가 발생했습니다.' })
    } finally {
      setExporting(false)
    }
  }

  const handlePatientToggle = (id: number) => {
    setSelectedPatientIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    )
    setQueryEnabled(false)
  }

  const handleItemToggle = (code: string) => {
    setSelectedItems((prev) =>
      prev.includes(code) ? prev.filter((x) => x !== code) : [...prev, code]
    )
    setQueryEnabled(false)
  }

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>📊</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>데이터 현황 집계</h1>
      </div>

      {/* 조회 조건 패널 */}
      <div style={{ background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: '14px 16px' }}>
        <div style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 12, paddingBottom: 8, borderBottom: '1px solid var(--gray-border)' }}>☰ 조회 조건</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12, alignItems: 'end' }}>
          {/* 조회 기간 */}
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 5 }}>
              조회 기간 <span style={{ color: 'var(--danger)' }}>*</span>
            </div>
            <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <input type="date" className="form-input" value={startDate} onChange={(e) => { setStartDate(e.target.value); setQueryEnabled(false) }} style={{ width: 130 }} />
              <span style={{ color: 'var(--text-sub)', fontSize: 12 }}>~</span>
              <input type="date" className="form-input" value={endDate} onChange={(e) => { setEndDate(e.target.value); setQueryEnabled(false) }} style={{ width: 130 }} />
            </div>
            {dateError && <p style={{ fontSize: 11, color: 'var(--danger)', marginTop: 4 }}>{dateError}</p>}
          </div>

          {/* 환자 선택 */}
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 5 }}>환자 선택</div>
            <div style={{ position: 'relative' }}>
              <details style={{ position: 'relative' }}>
                <summary className="form-input" style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', listStyle: 'none' }}>
                  {selectedPatientIds.length === 0 ? '전체 환자' : `${selectedPatientIds.length}명 선택`}
                </summary>
                <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 5, maxHeight: 200, overflowY: 'auto', zIndex: 10, padding: 4 }}>
                  {patients.map((p) => (
                    <label key={p.id} style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '4px 8px', fontSize: 12, cursor: 'pointer' }}>
                      <input type="checkbox" checked={selectedPatientIds.includes(p.id)} onChange={() => handlePatientToggle(p.id)} />
                      {p.patientCode} {p.name}
                    </label>
                  ))}
                </div>
              </details>
            </div>
          </div>

          {/* 항목 선택 + 조회 버튼 */}
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-sub)', marginBottom: 5 }}>수집 항목</div>
            <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <div style={{ position: 'relative', flex: 1 }}>
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
              <Button variant="primary" onClick={handleSearch} loading={isFetching}>조회</Button>
            </div>
          </div>
        </div>
      </div>

      {/* 집계 테이블 */}
      {queryEnabled && (
        <div className="table-panel">
          <div style={{ padding: '11px 16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--gray-border)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <span style={{ fontSize: 13, fontWeight: 600 }}>일별 집계</span>
              <span style={{ fontSize: 11.5, color: 'var(--text-sub)' }}>
                총 <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{rows.length}</span>건
              </span>
            </div>
            <Button variant="outline" size="sm" onClick={handleExport} loading={exporting} disabled={exporting}>
              📥 엑셀 다운로드
            </Button>
          </div>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: 800 }}>
              <thead>
                <tr>
                  <th>환자코드</th>
                  <th>환자명</th>
                  <th>날짜</th>
                  {displayItems.map((ic) => (
                    <th key={ic.code} style={{ textAlign: 'center' }}>{ic.label}<br /><span style={{ fontWeight: 400, fontSize: 10, color: 'var(--text-sub)' }}>건수/평균</span></th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={3 + displayItems.length} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>로딩 중...</td></tr>
                ) : pagedRows.length === 0 ? (
                  <tr><td colSpan={3 + displayItems.length} style={{ textAlign: 'center', padding: 40, color: 'var(--text-sub)' }}>데이터가 없습니다.</td></tr>
                ) : (
                  <>
                    {pagedRows.map((row) => (
                      <tr key={`${row.patientId}_${row.date}`} style={{ cursor: 'pointer' }} onClick={() => navigate(`/history/biometric?patientId=${row.patientId}&start=${row.date}&end=${row.date}`)}>
                        <td style={{ fontWeight: 600, color: 'var(--primary)' }}>{row.patientCode}</td>
                        <td>{row.patientName}</td>
                        <td style={{ color: 'var(--text-sub)', fontSize: 12 }}>{row.date}</td>
                        {displayItems.map((ic) => {
                          const val = row.items.get(ic.code)
                          if (!val || val.count === 0) {
                            return <td key={ic.code} style={{ textAlign: 'center', color: 'var(--text-sub)' }}>-</td>
                          }
                          return (
                            <td key={ic.code} style={{ textAlign: 'center', fontSize: 12 }}>
                              <span style={{ fontWeight: 600 }}>{val.count}</span>
                              <span style={{ color: 'var(--text-sub)', margin: '0 2px' }}>/</span>
                              <span>{val.avg != null ? val.avg.toFixed(1) : '-'}</span>
                            </td>
                          )
                        })}
                      </tr>
                    ))}
                    {/* 합계 행 */}
                    {rows.length > 0 && page === totalPages - 1 && (
                      <tr style={{ background: 'var(--tbl-header)', fontWeight: 600 }}>
                        <td colSpan={3} style={{ fontWeight: 600 }}>합계</td>
                        {displayItems.map((ic) => {
                          const t = totals.get(ic.code)
                          if (!t || t.count === 0) return <td key={ic.code} style={{ textAlign: 'center', color: 'var(--text-sub)' }}>-</td>
                          const avg = t.sum / t.count
                          return (
                            <td key={ic.code} style={{ textAlign: 'center', fontSize: 12 }}>
                              <span style={{ fontWeight: 600 }}>{t.count}</span>
                              <span style={{ color: 'var(--text-sub)', margin: '0 2px' }}>/</span>
                              <span>{avg.toFixed(1)}</span>
                            </td>
                          )
                        })}
                      </tr>
                    )}
                  </>
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
