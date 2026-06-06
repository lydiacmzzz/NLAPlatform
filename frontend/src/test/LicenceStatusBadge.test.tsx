import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { LicenceStatusBadge } from '../components/centres/LicenceStatusBadge'

describe('LicenceStatusBadge', () => {
  it('renders Active label', () => {
    render(<LicenceStatusBadge status="ACTIVE" />)
    expect(screen.getByText('Active')).toBeInTheDocument()
  })

  it('renders Pending Renewal label', () => {
    render(<LicenceStatusBadge status="PENDING_RENEWAL" />)
    expect(screen.getByText('Pending Renewal')).toBeInTheDocument()
  })

  it('renders Suspended label', () => {
    render(<LicenceStatusBadge status="SUSPENDED" />)
    expect(screen.getByText('Suspended')).toBeInTheDocument()
  })

  it('renders Expired label', () => {
    render(<LicenceStatusBadge status="EXPIRED" />)
    expect(screen.getByText('Expired')).toBeInTheDocument()
  })
})
