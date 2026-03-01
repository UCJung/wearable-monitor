import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { deviceApi } from '@/api/deviceApi'
import { assignmentApi } from '@/api/assignmentApi'
import { useToastStore } from '@/stores/toastStore'
import type { DeviceSearchCondition, CreateDeviceRequest, UpdateDeviceRequest } from '@/types/device'
import type { AssignDeviceRequest, ReturnDeviceRequest } from '@/types/assignment'

export function useDeviceList(condition: DeviceSearchCondition) {
  return useQuery({
    queryKey: ['devices', condition],
    queryFn: () => deviceApi.getDevices(condition).then((r) => r.data.data!),
  })
}

export function useDeviceDetail(id: number) {
  return useQuery({
    queryKey: ['device', id],
    queryFn: () => deviceApi.getDevice(id).then((r) => r.data.data!),
    enabled: id > 0,
  })
}

export function useCreateDevice() {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: (data: CreateDeviceRequest) => deviceApi.createDevice(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['devices'] })
      toast.push({ type: 'success', title: '장치 등록 완료', message: '새 장치가 등록되었습니다.' })
    },
    onError: () => {
      toast.push({ type: 'danger', title: '등록 실패', message: '장치 등록 중 오류가 발생했습니다.' })
    },
  })
}

export function useUpdateDevice(id: number) {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: (data: UpdateDeviceRequest) => deviceApi.updateDevice(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['devices'] })
      qc.invalidateQueries({ queryKey: ['device', id] })
      toast.push({ type: 'success', title: '수정 완료', message: '장치 정보가 수정되었습니다.' })
    },
    onError: () => {
      toast.push({ type: 'danger', title: '수정 실패', message: '장치 정보 수정 중 오류가 발생했습니다.' })
    },
  })
}

export function useDeleteDevice() {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: (id: number) => deviceApi.deleteDevice(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['devices'] })
      toast.push({ type: 'success', title: '삭제 완료', message: '장치가 삭제되었습니다.' })
    },
    onError: (err: unknown) => {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        '장치 삭제 중 오류가 발생했습니다.'
      toast.push({ type: 'danger', title: '삭제 실패', message: msg })
    },
  })
}

export function useAssignDevice() {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: (data: AssignDeviceRequest) => assignmentApi.assignDevice(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['patients'] })
      qc.invalidateQueries({ queryKey: ['patient'] })
      qc.invalidateQueries({ queryKey: ['devices'] })
      qc.invalidateQueries({ queryKey: ['assignments'] })
      toast.push({ type: 'success', title: '장치 할당 완료', message: '장치가 환자에게 할당되었습니다.' })
    },
    onError: (err: unknown) => {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        '장치 할당 중 오류가 발생했습니다.'
      toast.push({ type: 'danger', title: '할당 실패', message: msg })
    },
  })
}

export function useReturnDevice() {
  const qc = useQueryClient()
  const toast = useToastStore()

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: ReturnDeviceRequest }) =>
      assignmentApi.returnDevice(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['patients'] })
      qc.invalidateQueries({ queryKey: ['patient'] })
      qc.invalidateQueries({ queryKey: ['devices'] })
      qc.invalidateQueries({ queryKey: ['assignments'] })
      toast.push({ type: 'success', title: '장치 해제 완료', message: '장치가 해제되었습니다.' })
    },
    onError: () => {
      toast.push({ type: 'danger', title: '해제 실패', message: '장치 해제 중 오류가 발생했습니다.' })
    },
  })
}
