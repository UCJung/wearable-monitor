import axiosInstance from './axiosInstance'

export const exportApi = {
  downloadPatientExcel(patientId: number, params?: { start?: string; end?: string }) {
    return axiosInstance.get(`/export/patient/${patientId}`, {
      params,
      responseType: 'blob',
    })
  },

  downloadAllExcel(params?: { start?: string; end?: string }) {
    return axiosInstance.get('/export/all', {
      params,
      responseType: 'blob',
    })
  },
}
