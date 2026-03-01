import axiosInstance from './axiosInstance'
import type { ApiResponse, PageResponse } from '@/types/common'
import type {
  PatientListResponse,
  PatientDetailResponse,
  PatientSearchCondition,
  CreatePatientRequest,
  UpdatePatientRequest,
} from '@/types/patient'

export const patientApi = {
  getPatients(params: PatientSearchCondition) {
    return axiosInstance.get<ApiResponse<PageResponse<PatientListResponse>>>('/patients', { params })
  },

  getPatient(id: number) {
    return axiosInstance.get<ApiResponse<PatientDetailResponse>>(`/patients/${id}`)
  },

  createPatient(data: CreatePatientRequest) {
    return axiosInstance.post<ApiResponse<PatientDetailResponse>>('/patients', data)
  },

  updatePatient(id: number, data: UpdatePatientRequest) {
    return axiosInstance.put<ApiResponse<PatientDetailResponse>>(`/patients/${id}`, data)
  },

  deletePatient(id: number) {
    return axiosInstance.delete<ApiResponse<void>>(`/patients/${id}`)
  },
}
