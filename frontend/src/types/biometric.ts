// === 수집 이력 ===
export interface BiometricRecord {
  id: number
  itemCode: string
  itemNameKo: string
  category: string
  measuredAt: string
  valueNumeric: number | null
  valueText: string | null
  unit: string | null
  durationSec: number | null
}

export interface BiometricHistoryResponse {
  records: BiometricRecord[]
  totalCount: number
  hasMore: boolean
}

export interface BiometricSearchCondition {
  itemCodes?: string[]
  start?: string
  end?: string
  page?: number
  size?: number
}

// === 모니터링: 할당 현황 ===
export interface SummaryCount {
  total: number
  assigned: number
  collecting: number
  notCollecting: number
}

export interface AssignmentStatusItem {
  patientId: number
  patientCode: string
  patientName: string
  status: string
  deviceId: number | null
  serialNumber: string | null
  modelName: string | null
  batteryLevel: number | null
  lastCollectedAt: string | null
  recentlyCollected: boolean
}

export interface MonitoringSummaryResponse {
  summary: SummaryCount
  patients: AssignmentStatusItem[]
}

// === 모니터링: 일별 집계 ===
export interface DailySummaryItem {
  patientId: number
  itemCode: string
  measureDate: string
  avgValue: number | null
  minValue: number | null
  maxValue: number | null
  measureCount: number
}

export interface DailySummaryResponse {
  items: DailySummaryItem[]
}

export interface DailySummaryCondition {
  patientIds?: number[]
  itemCodes?: string[]
  start?: string
  end?: string
}

// === 수집 항목 정의 ===
export interface CollectionItemDefinition {
  id: number
  itemCode: string
  itemNameKo: string
  category: 'VITAL_SIGN' | 'ACTIVITY' | 'SLEEP' | 'AI_SCORE'
  unit: string | null
  displayOrder: number
}
