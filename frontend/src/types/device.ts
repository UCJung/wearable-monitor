export type DeviceStatus = 'ACTIVE' | 'RETIRED'

export interface DeviceListResponse {
  id: number
  serialNumber: string
  modelName: string
  deviceStatus: DeviceStatus
  batteryLevel: number | null
  lastSyncAt: string | null
  assignedPatientName: string | null
  createdAt: string
}

export interface DeviceCurrentAssignment {
  assignmentId: number
  patientId: number
  patientCode: string
  patientName: string
  monitoringStartDate: string
  assignmentStatus: string
}

export interface DeviceAssignmentHistory {
  assignmentId: number
  patientName: string
  monitoringStartDate: string
  monitoringEndDate: string | null
  assignmentStatus: string
  assignedAt: string
  returnedAt: string | null
}

export interface DeviceDetailResponse {
  id: number
  serialNumber: string
  modelName: string
  deviceStatus: DeviceStatus
  batteryLevel: number | null
  lastSyncAt: string | null
  createdAt: string
  currentAssignment: DeviceCurrentAssignment | null
  assignmentHistory: DeviceAssignmentHistory[]
}

export interface DeviceSearchCondition {
  keyword?: string
  deviceStatus?: DeviceStatus
  page?: number
  size?: number
}

export interface CreateDeviceRequest {
  serialNumber: string
  modelName?: string
}

export interface UpdateDeviceRequest {
  modelName?: string
  batteryLevel?: number
}
