import { renderHook, waitFor, act } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useCentreSearch } from '../hooks/useCentreSearch'
import { centreService } from '../services/centreService'
import type { PagedResponse, CentreSummaryDto } from '../types/centre'

vi.mock('../services/centreService')

const mockSearchResult: PagedResponse<CentreSummaryDto> = {
  content: [
    {
      id: 1,
      centreId: 'CC-001',
      name: 'Happy Kids Centre',
      centreType: 'CHILD_CARE',
      licenceStatus: 'ACTIVE',
    },
    {
      id: 2,
      centreId: 'CC-002',
      name: 'Smart Minds Centre',
      centreType: 'STUDENT_CARE',
      licenceStatus: 'PENDING_RENEWAL',
    },
  ],
  totalElements: 2,
  totalPages: 1,
  currentPage: 0,
  pageSize: 20,
}

describe('useCentreSearch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should perform search on mount with default params', async () => {
    vi.mocked(centreService.search).mockResolvedValue(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    expect(result.current.loading).toBe(true)

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.result).toEqual(mockSearchResult)
    expect(result.current.error).toBeNull()
    expect(centreService.search).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sortBy: 'updatedAt',
      sortDir: 'desc',
    })
  })

  it('should return empty results when search returns no items', async () => {
    const emptyResult: PagedResponse<CentreSummaryDto> = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      currentPage: 0,
      pageSize: 20,
    }
    vi.mocked(centreService.search).mockResolvedValue(emptyResult)

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.result?.content).toHaveLength(0)
  })

  it('should handle search error', async () => {
    vi.mocked(centreService.search).mockRejectedValue(new Error('API error'))

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.error).toBe('Failed to load centres.')
    expect(result.current.result).toBeNull()
  })

  it('should update search params and refetch', async () => {
    vi.mocked(centreService.search).mockResolvedValue(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    act(() => {
      result.current.updateParams({ name: 'Happy', status: 'ACTIVE' })
    })

    await waitFor(() => {
      expect(centreService.search).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'Happy',
          status: 'ACTIVE',
          page: 0, // Should reset to first page
        })
      )
    })
  })

  it('should reset page to 0 when updating params', async () => {
    vi.mocked(centreService.search).mockResolvedValue(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    // Simulate being on page 3
    act(() => {
      result.current.goToPage(3)
    })

    await waitFor(() => {
      expect(result.current.params.page).toBe(3)
    })

    // Update params should reset to page 0
    act(() => {
      result.current.updateParams({ name: 'New Search' })
    })

    await waitFor(() => {
      expect(result.current.params.page).toBe(0)
    })
  })

  it('should handle pagination', async () => {
    vi.mocked(centreService.search).mockResolvedValue(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    act(() => {
      result.current.goToPage(2)
    })

    await waitFor(() => {
      expect(result.current.params.page).toBe(2)
      expect(centreService.search).toHaveBeenCalledWith(
        expect.objectContaining({ page: 2 })
      )
    })
  })

  it('should preserve other params when navigating pages', async () => {
    vi.mocked(centreService.search).mockResolvedValue(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    act(() => {
      result.current.updateParams({
        name: 'Happy',
        sortBy: 'name',
        sortDir: 'asc',
      })
    })

    await waitFor(() => {
      expect(centreService.search).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'Happy',
          sortBy: 'name',
          sortDir: 'asc',
          page: 0,
        })
      )
    })

    act(() => {
      result.current.goToPage(1)
    })

    await waitFor(() => {
      expect(centreService.search).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'Happy',
          sortBy: 'name',
          sortDir: 'asc',
          page: 1,
        })
      )
    })
  })

  it('should clear error on successful search', async () => {
    vi.mocked(centreService.search)
      .mockRejectedValueOnce(new Error('Error'))
      .mockResolvedValueOnce(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.error).toBe('Failed to load centres.')
    })

    act(() => {
      result.current.updateParams({ name: 'Retry' })
    })

    await waitFor(() => {
      expect(result.current.error).toBeNull()
      expect(result.current.result).toEqual(mockSearchResult)
    })
  })

  it('should maintain default params on first render', () => {
    vi.mocked(centreService.search).mockResolvedValue(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    expect(result.current.params).toEqual({
      page: 0,
      size: 20,
      sortBy: 'updatedAt',
      sortDir: 'desc',
    })
  })

  it('should handle multiple param updates', async () => {
    vi.mocked(centreService.search).mockResolvedValue(mockSearchResult)

    const { result } = renderHook(() => useCentreSearch())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    act(() => {
      result.current.updateParams({ name: 'Test' })
    })

    act(() => {
      result.current.updateParams({ centreType: 'CHILD_CARE' })
    })

    await waitFor(() => {
      expect(result.current.params).toMatchObject({
        name: 'Test',
        centreType: 'CHILD_CARE',
        page: 0,
      })
    })
  })
})
