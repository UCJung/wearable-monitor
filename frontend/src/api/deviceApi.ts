import axiosInstance from './axiosInstance'
import type { ApiResponse, PageResponse } from '@/types/common'
import type {
  DeviceListResponse,
  DeviceDetailResponse,
  DeviceSearchCondition,
  CreateDeviceRequest,
  UpdateDeviceRequest,
} from '@/types/device'

export const deviceApi = {
  getDevices(params: DeviceSearchCondition) {
    return axiosInstance.get<ApiResponse<PageResponse<DeviceListResponse>>>('/devices', { params })
  },

  getDevice(id: number) {
    return axiosInstance.get<ApiResponse<DeviceDetailResponse>>(`/devices/${id}`)
  },

  createDevice(data: CreateDeviceRequest) {
    return axiosInstance.post<ApiResponse<DeviceDetailResponse>>('/devices', data)
  },

  updateDevice(id: number, data: UpdateDeviceRequest) {
    return axiosInstance.put<ApiResponse<DeviceDetailResponse>>(`/devices/${id}`, data)
  },

  deleteDevice(id: number) {
    return axiosInstance.delete<ApiResponse<void>>(`/devices/${id}`)
  },
}
