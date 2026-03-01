import { useQuery } from '@tanstack/react-query'
import { monitoringApi } from '@/api/monitoringApi'
import type { DailySummaryCondition } from '@/types/biometric'

export function useDailySummary(condition: DailySummaryCondition, enabled: boolean) {
  return useQuery({
    queryKey: ['monitoring', 'daily-summary', condition],
    queryFn: () => monitoringApi.getDailySummary(condition).then((r) => r.data.data!),
    enabled,
  })
}
