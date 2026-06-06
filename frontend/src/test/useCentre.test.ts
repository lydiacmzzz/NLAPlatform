import { renderHook, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useCentre } from '../hooks/useCentre'
import { centreService } from '../services/centreService'
import type { CentreProfileDto, UpdateCentreRequest } from '../types/centre'

vi.mock('../services/centreService')

const mockCentreData: CentreProfileDto = {
  id: 1,
  centreId: 'CC-001',
  name: 'Happy Kids Centre',
  address: '123 Test Street',
  postalCode: '123456',
  centreType: 'CHILD_CARE',
  licenceStatus: 'ACTIVE',
  licenceNumber: 'LIC-001',
  licenceExpiryDate: '2025-12-31',
  capacity: 50,
  operatingHours: '7am-7pm',
  contacts: [],
  kahDetails: [],
  lifecycleEvents: [],
}

describe('useCentre', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should fetch centre data on mount', async () => {
    vi.mocked(centreService.getById).mockResolvedValue(mockCentreData)

    const { result } = renderHook(() => useCentre(1))

    expect(result.current.loading).toBe(true)
    expect(result.current.centre).toBeNull()

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.centre).toEqual(mockCentreData)
    expect(result.current.error).toBeNull()
    expect(centreService.getById).toHaveBeenCalledWith(1)
  })

  it('should handle 404 error when centre not found', async () => {
    vi.mocked(centreService.getById).mockRejectedValue(new Error('Centre not found'))

    const { result } = renderHook(() => useCentre(999))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.error).toBe('Failed to load centre details.')
    expect(result.current.centre).toBeNull()
  })

  it('should handle network error', async () => {
    vi.mocked(centreService.getById).mockRejectedValue(new Error('Network error'))

    const { result } = renderHook(() => useCentre(1))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.error).toBe('Failed to load centre details.')
  })

  it('should refetch when id changes', async () => {
    const data1 = { ...mockCentreData, id: 1, name: 'Centre 1' }
    const data2 = { ...mockCentreData, id: 2, name: 'Centre 2' }

    vi.mocked(centreService.getById).mockResolvedValueOnce(data1).mockResolvedValueOnce(data2)

    const { result, rerender } = renderHook(({ id }) => useCentre(id), {
      initialProps: { id: 1 },
    })

    await waitFor(() => {
      expect(result.current.centre?.name).toBe('Centre 1')
    })

    rerender({ id: 2 })

    await waitFor(() => {
      expect(result.current.centre?.name).toBe('Centre 2')
    })

    expect(centreService.getById).toHaveBeenCalledTimes(2)
    expect(centreService.getById).toHaveBeenNthCalledWith(1, 1)
    expect(centreService.getById).toHaveBeenNthCalledWith(2, 2)
  })

  it('should update centre and return true on success', async () => {
    const updatedData = { ...mockCentreData, name: 'Updated Centre' }
    const updateRequest: UpdateCentreRequest = { name: 'Updated Centre' }

    vi.mocked(centreService.getById).mockResolvedValue(mockCentreData)
    vi.mocked(centreService.update).mockResolvedValue(updatedData)

    const { result } = renderHook(() => useCentre(1))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    const updateResult = await result.current.update(updateRequest)

    expect(updateResult).toBe(true)
    await waitFor(() => {
      expect(result.current.centre).toEqual(updatedData)
      expect(result.current.saving).toBe(false)
    })
    expect(centreService.update).toHaveBeenCalledWith(1, updateRequest)
  })

  it('should handle update error and return false', async () => {
    vi.mocked(centreService.getById).mockResolvedValue(mockCentreData)
    vi.mocked(centreService.update).mockRejectedValue(new Error('Update failed'))

    const { result } = renderHook(() => useCentre(1))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    const updateRequest: UpdateCentreRequest = { name: 'New Name' }
    const updateResult = await result.current.update(updateRequest)

    expect(updateResult).toBe(false)
    await waitFor(() => {
      expect(result.current.error).toBe('Failed to save changes.')
      expect(result.current.saving).toBe(false)
    })
  })

  it('should set saving=true during update', async () => {
    vi.mocked(centreService.getById).mockResolvedValue(mockCentreData)
    vi.mocked(centreService.update).mockImplementation(
      () =>
        new Promise((resolve) => {
          setTimeout(() => resolve(mockCentreData), 100)
        })
    )

    const { result } = renderHook(() => useCentre(1))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    const updatePromise = result.current.update({})

    await waitFor(() => {
      expect(result.current.saving).toBe(true)
    })

    await updatePromise
    await waitFor(() => {
      expect(result.current.saving).toBe(false)
    })
  })

  it('should manually reload centre data', async () => {
    vi.mocked(centreService.getById).mockResolvedValue(mockCentreData)

    const { result } = renderHook(() => useCentre(1))

    await waitFor(() => {
      expect(result.current.centre).toBeDefined()
    })

    expect(centreService.getById).toHaveBeenCalledTimes(1)

    await result.current.reload()

    expect(centreService.getById).toHaveBeenCalledTimes(2)
  })

  it('should clear error when loading succeeds', async () => {
    vi.mocked(centreService.getById)
      .mockRejectedValueOnce(new Error('First error'))
      .mockResolvedValueOnce(mockCentreData)

    const { result, rerender } = renderHook(() => useCentre(1))

    await waitFor(() => {
      expect(result.current.error).toBe('Failed to load centre details.')
    })

    await result.current.reload()

    await waitFor(() => {
      expect(result.current.error).toBeNull()
      expect(result.current.centre).toEqual(mockCentreData)
    })
  })
})
