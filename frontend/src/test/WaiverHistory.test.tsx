import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { WaiverHistory } from '../components/centres/WaiverHistory'
import type { WaiverHistoryDto } from '../types/centre'

const approvedWaiver: WaiverHistoryDto = {
  id: 1,
  waiverType: 'Physical Environment',
  waiverTitle: 'Outdoor Play Area Waiver',
  waiverDescription: 'Waiver for outdoor play area requirement',
  waiverStatus: 'APPROVED',
  approvalDate: '2023-03-15',
  expiryDate: '2025-03-14',
  approvedBy: 'officer1',
  officerRemarks: 'Approved with conditions',
  supportingDocumentName: 'outdoor_waiver.pdf',
  supportingDocumentUrl: 'https://docs.ecda.gov.sg/waivers/outdoor_waiver.pdf',
}

const expiredWaiver: WaiverHistoryDto = {
  id: 2,
  waiverType: 'Operating Capacity',
  waiverTitle: 'Capacity Exception Waiver',
  waiverDescription: null,
  waiverStatus: 'EXPIRED',
  approvalDate: '2021-09-01',
  expiryDate: '2022-02-28',
  approvedBy: 'admin',
  officerRemarks: 'One-time measure only',
  supportingDocumentName: null,
  supportingDocumentUrl: null,
}

const rejectedWaiver: WaiverHistoryDto = {
  id: 3,
  waiverType: 'Staffing',
  waiverTitle: 'Reduced Staff Ratio Waiver',
  waiverDescription: null,
  waiverStatus: 'REJECTED',
  approvalDate: null,
  expiryDate: null,
  approvedBy: null,
  officerRemarks: 'Rejected — core safety standard cannot be waived.',
  supportingDocumentName: null,
  supportingDocumentUrl: null,
}

describe('WaiverHistory', () => {
  it('renders empty state when no waivers exist', () => {
    render(<WaiverHistory waivers={[]} />)
    expect(screen.getByText('No waiver history found for this centre.')).toBeInTheDocument()
  })

  it('renders waiver title and type', () => {
    render(<WaiverHistory waivers={[approvedWaiver]} />)
    expect(screen.getByText('Outdoor Play Area Waiver')).toBeInTheDocument()
    expect(screen.getByText('Physical Environment')).toBeInTheDocument()
  })

  it('renders waiver status', () => {
    render(<WaiverHistory waivers={[approvedWaiver, expiredWaiver]} />)
    expect(screen.getByText('APPROVED')).toBeInTheDocument()
    expect(screen.getByText('EXPIRED')).toBeInTheDocument()
  })

  it('renders approved by and officer remarks', () => {
    render(<WaiverHistory waivers={[approvedWaiver]} />)
    expect(screen.getByText('officer1')).toBeInTheDocument()
    expect(screen.getByText('Approved with conditions')).toBeInTheDocument()
  })

  it('renders supporting document link when provided', () => {
    render(<WaiverHistory waivers={[approvedWaiver]} />)
    const link = screen.getByText('outdoor_waiver.pdf')
    expect(link).toBeInTheDocument()
    expect(link.closest('a')).toHaveAttribute('href', 'https://docs.ecda.gov.sg/waivers/outdoor_waiver.pdf')
  })

  it('does not render document link when not provided', () => {
    render(<WaiverHistory waivers={[expiredWaiver]} />)
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
  })

  it('renders multiple waivers', () => {
    render(<WaiverHistory waivers={[approvedWaiver, expiredWaiver, rejectedWaiver]} />)
    expect(screen.getByText('Outdoor Play Area Waiver')).toBeInTheDocument()
    expect(screen.getByText('Capacity Exception Waiver')).toBeInTheDocument()
    expect(screen.getByText('Reduced Staff Ratio Waiver')).toBeInTheDocument()
  })

  it('renders rejection status correctly', () => {
    render(<WaiverHistory waivers={[rejectedWaiver]} />)
    expect(screen.getByText('REJECTED')).toBeInTheDocument()
  })

  it('does not render any add, edit, or delete buttons', () => {
    render(<WaiverHistory waivers={[approvedWaiver]} />)
    expect(screen.queryByRole('button', { name: /add/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /edit/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument()
  })

  it('renders section heading', () => {
    render(<WaiverHistory waivers={[]} />)
    expect(screen.getByText('Waiver History')).toBeInTheDocument()
  })
})
