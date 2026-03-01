import { useQuery } from '@tanstack/react-query'
import { biometricApi } from '@/api/biometricApi'
import type { BiometricSearchCondition } from '@/types/biometric'

export function useBiometricHistory(patientId: number, condition: BiometricSearchCondition) {
  return useQuery({
    queryKey: ['biometric', patientId, condition],
    queryFn: () => biometricApi.getBiometricHistory(patientId, condition).then((r) => r.data.data!),
    enabled: patientId > 0,
  })
}
