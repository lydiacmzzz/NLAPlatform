import { useState, useEffect, useCallback } from 'react'
import { centreService } from '../services/centreService'
import type { CentreProfileDto, UpdateCentreRequest } from '../types/centre'

export function useCentre(id: number) {
  const [centre, setCentre] = useState<CentreProfileDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setCentre(await centreService.getById(id))
    } catch {
      setError('Failed to load centre details.')
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => { load() }, [load])

  const update = useCallback(async (req: UpdateCentreRequest): Promise<boolean> => {
    setSaving(true)
    setError(null)
    try {
      const updated = await centreService.update(id, req)
      setCentre(updated)
      return true
    } catch {
      setError('Failed to save changes.')
      return false
    } finally {
      setSaving(false)
    }
  }, [id])

  return { centre, loading, error, saving, update, reload: load }
}
