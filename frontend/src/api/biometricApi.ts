import axiosInstance from './axiosInstance'
import type { ApiResponse } from '@/types/common'
import type { BiometricHistoryResponse, BiometricSearchCondition } from '@/types/biometric'

export const biometricApi = {
  getBiometricHistory(patientId: number, params: BiometricSearchCondition) {
    return axiosInstance.get<ApiResponse<BiometricHistoryResponse>>(`/biometric/${patientId}`, { params })
  },
}
