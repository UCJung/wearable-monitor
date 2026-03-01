export type AssignmentStatus = 'ACTIVE' | 'RETURNED' | 'EXPIRED'

export interface AssignmentListResponse {
  id: number
  patientId: number
  patientCode: string
  patientName: string
  deviceId: number
  serialNumber: string
  modelName: string
  assignmentStatus: AssignmentStatus
  monitoringStartDate: string
  monitoringEndDate: string | null
  assignedAt: string
  returnedAt: string | null
}

export interface AssignDeviceRequest {
  patientId: number
  deviceId: number
  startDate: string
  endDate?: string
}

export interface ReturnDeviceRequest {
  endDate: string
}

export interface AssignmentSearchCondition {
  keyword?: string
  status?: AssignmentStatus
  page?: number
  size?: number
}
