import axiosInstance from './axiosInstance'
import type { ApiResponse, PageResponse } from '@/types/common'
import type {
  AssignmentListResponse,
  AssignDeviceRequest,
  ReturnDeviceRequest,
  AssignmentSearchCondition,
} from '@/types/assignment'

export const assignmentApi = {
  getAssignments(params: AssignmentSearchCondition) {
    return axiosInstance.get<ApiResponse<PageResponse<AssignmentListResponse>>>('/assignments', { params })
  },

  getAssignment(id: number) {
    return axiosInstance.get<ApiResponse<AssignmentListResponse>>(`/assignments/${id}`)
  },

  assignDevice(data: AssignDeviceRequest) {
    return axiosInstance.post<ApiResponse<AssignmentListResponse>>('/assignments', data)
  },

  returnDevice(id: number, data: ReturnDeviceRequest) {
    return axiosInstance.put<ApiResponse<AssignmentListResponse>>(`/assignments/${id}/return`, data)
  },
}
