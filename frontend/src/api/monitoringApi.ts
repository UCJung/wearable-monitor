import axiosInstance from './axiosInstance'
import type { ApiResponse } from '@/types/common'
import type { MonitoringSummaryResponse, DailySummaryResponse, DailySummaryCondition } from '@/types/biometric'

export const monitoringApi = {
  getAssignmentStatus() {
    return axiosInstance.get<ApiResponse<MonitoringSummaryResponse>>('/monitoring/assignment-status')
  },

  getDailySummary(params: DailySummaryCondition) {
    return axiosInstance.get<ApiResponse<DailySummaryResponse>>('/monitoring/daily-summary', { params })
  },
}
