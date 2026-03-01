import axiosInstance from './axiosInstance'
import type { ApiResponse } from '@/types/common'

interface LoginRequest {
  username: string
  password: string
  platform?: string
}

interface LoginResponse {
  accessToken: string
  refreshToken: string
  role: string
  username: string
}

interface RefreshRequest {
  refreshToken: string
}

interface RefreshResponse {
  accessToken: string
}

export const authApi = {
  login(data: LoginRequest) {
    return axiosInstance.post<ApiResponse<LoginResponse>>('/auth/login', data)
  },

  refresh(data: RefreshRequest) {
    return axiosInstance.post<ApiResponse<RefreshResponse>>('/auth/refresh', data)
  },

  logout() {
    return axiosInstance.post<ApiResponse<null>>('/auth/logout')
  },
}
