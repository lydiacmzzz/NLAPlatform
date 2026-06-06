import { useState, useEffect } from 'react'
import { centreService } from '../services/centreService'
import type { WaiverHistoryDto } from '../types/centre'

export function useWaiverHistory(centreId: number, enabled: boolean) {
  const [waivers, setWaivers] = useState<WaiverHistoryDto[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!enabled || !centreId) return
    setLoading(true)
    setError(null)
    centreService.getWaivers(centreId)
      .then(setWaivers)
      .catch((e: Error) => setError(e.message ?? 'Failed to load waiver history'))
      .finally(() => setLoading(false))
  }, [centreId, enabled])

  return { waivers, loading, error }
}
