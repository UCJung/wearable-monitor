import { useState, useEffect, useRef, useCallback } from 'react'
import { useQuery } from '@tanstack/react-query'
import { monitoringApi } from '@/api/monitoringApi'

const REFRESH_INTERVAL = 300_000 // 5분

export function useAssignmentStatus() {
  return useQuery({
    queryKey: ['monitoring', 'assignment-status'],
    queryFn: () => monitoringApi.getAssignmentStatus().then((r) => r.data.data!),
    refetchInterval: REFRESH_INTERVAL,
  })
}

export function useCountdown(totalSeconds: number) {
  const [remaining, setRemaining] = useState(totalSeconds)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const reset = useCallback(() => {
    setRemaining(totalSeconds)
  }, [totalSeconds])

  useEffect(() => {
    intervalRef.current = setInterval(() => {
      setRemaining((prev) => {
        if (prev <= 1) return totalSeconds
        return prev - 1
      })
    }, 1000)

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current)
    }
  }, [totalSeconds])

  const minutes = Math.floor(remaining / 60)
  const seconds = remaining % 60
  const display = `${minutes}분 ${String(seconds).padStart(2, '0')}초`

  return { remaining, display, reset }
}
