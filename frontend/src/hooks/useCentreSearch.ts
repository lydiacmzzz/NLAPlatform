import { useState, useEffect, useCallback } from 'react'
import { centreService } from '../services/centreService'
import type { CentreSummaryDto, CentreSearchParams, PagedResponse } from '../types/centre'

const DEFAULT_PARAMS: CentreSearchParams = { page: 0, size: 20, sortBy: 'updatedAt', sortDir: 'desc' }

export function useCentreSearch() {
  const [params, setParams] = useState<CentreSearchParams>(DEFAULT_PARAMS)
  const [result, setResult] = useState<PagedResponse<CentreSummaryDto> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const search = useCallback(async (p: CentreSearchParams) => {
    setLoading(true)
    setError(null)
    try {
      setResult(await centreService.search(p))
    } catch {
      setError('Failed to load centres.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { search(params) }, [params, search])

  const updateParams = useCallback((updates: Partial<CentreSearchParams>) => {
    setParams((prev) => ({ ...prev, ...updates, page: 0 }))
  }, [])

  const goToPage = useCallback((page: number) => {
    setParams((prev) => ({ ...prev, page }))
  }, [])

  return { result, loading, error, params, updateParams, goToPage }
}
