import api from './api'
import type {
  CentreProfileDto,
  CentreSummaryDto,
  CentreSearchParams,
  PagedResponse,
  UpdateCentreRequest,
  CreateKahRequest,
  KahDetailDto,
  WaiverHistoryDto,
} from '../types/centre'

export const centreService = {
  async search(params: CentreSearchParams): Promise<PagedResponse<CentreSummaryDto>> {
    const { data } = await api.get('/centres', { params })
    return data
  },

  async getById(id: number): Promise<CentreProfileDto> {
    const { data } = await api.get(`/centres/${id}`)
    return data
  },

  async update(id: number, req: UpdateCentreRequest): Promise<CentreProfileDto> {
    const { data } = await api.patch(`/centres/${id}`, req)
    return data
  },

  async getKahHistory(centreId: number): Promise<KahDetailDto[]> {
    const { data } = await api.get(`/centres/${centreId}/kah`)
    return data
  },

  async addKah(centreId: number, req: CreateKahRequest): Promise<KahDetailDto> {
    const { data } = await api.post(`/centres/${centreId}/kah`, req)
    return data
  },

  async updateKah(centreId: number, kahId: number, req: Partial<CreateKahRequest>): Promise<KahDetailDto> {
    const { data } = await api.patch(`/centres/${centreId}/kah/${kahId}`, req)
    return data
  },

  async getWaivers(centreId: number): Promise<WaiverHistoryDto[]> {
    const { data } = await api.get(`/centres/${centreId}/waivers`)
    return data
  },
}
