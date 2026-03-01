import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { patientApi } from '@/api/patientApi'
import { useToastStore } from '@/stores/toastStore'
import type { PatientSearchCondition, CreatePatientRequest, UpdatePatientRequest } from '@/types/patient'

export function usePatientList(condition: PatientSearchCondition) {
  return useQuery({
    queryKey: ['patients', condition],
    queryFn: () => patientApi.getPatients(condition).then((r) => r.data.data!),
  })
}

export function usePatientDetail(id: number) {
  return useQuery({
    queryKey: ['patient', id],
    queryFn: () => patientApi.getPatient(id).then((r) => r.data.data!),
    enabled: id > 0,
  })
}

export function useCreatePatient() {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: (data: CreatePatientRequest) => patientApi.createPatient(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['patients'] })
      toast.push({ type: 'success', title: '환자 등록 완료', message: '새 환자가 등록되었습니다.' })
    },
    onError: () => {
      toast.push({ type: 'danger', title: '등록 실패', message: '환자 등록 중 오류가 발생했습니다.' })
    },
  })
}

export function useUpdatePatient(id: number) {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: (data: UpdatePatientRequest) => patientApi.updatePatient(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['patients'] })
      qc.invalidateQueries({ queryKey: ['patient', id] })
      toast.push({ type: 'success', title: '수정 완료', message: '환자 정보가 수정되었습니다.' })
    },
    onError: () => {
      toast.push({ type: 'danger', title: '수정 실패', message: '환자 정보 수정 중 오류가 발생했습니다.' })
    },
  })
}

export function useDeletePatient() {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: (id: number) => patientApi.deletePatient(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['patients'] })
      toast.push({ type: 'success', title: '삭제 완료', message: '환자가 삭제되었습니다.' })
    },
    onError: (err: unknown) => {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        '환자 삭제 중 오류가 발생했습니다.'
      toast.push({ type: 'danger', title: '삭제 실패', message: msg })
    },
  })
}
