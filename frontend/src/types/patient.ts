export type PatientStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED'

export interface PatientListResponse {
  id: number
  patientCode: string
  name: string
  birthDate: string
  gender: 'M' | 'F'
  status: PatientStatus
  hasDevice: boolean
  createdAt: string
}

export interface CurrentDevice {
  assignmentId: number
  deviceId: number
  serialNumber: string
  modelName: string
  monitoringStartDate: string
  assignmentStatus: string
}

export interface AssignmentHistory {
  assignmentId: number
  serialNumber: string
  monitoringStartDate: string
  monitoringEndDate: string | null
  assignmentStatus: string
  assignedAt: string
  returnedAt: string | null
}

export interface PatientDetailResponse {
  id: number
  patientCode: string
  name: string
  birthDate: string
  gender: 'M' | 'F'
  notes: string | null
  status: PatientStatus
  createdAt: string
  currentDevice: CurrentDevice | null
  assignmentHistory: AssignmentHistory[]
}

export interface PatientSearchCondition {
  name?: string
  patientCode?: string
  status?: PatientStatus
  hasDevice?: boolean
  page?: number
  size?: number
}

export interface CreatePatientRequest {
  name: string
  birthDate: string
  gender: 'M' | 'F'
  notes?: string
}

export interface UpdatePatientRequest {
  name?: string
  birthDate?: string
  gender?: 'M' | 'F'
  notes?: string
}
