import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { CentreLifecycle } from '../components/centres/CentreLifecycle'
import type { LifecycleEventDto } from '../types/centre'

const events: LifecycleEventDto[] = [
  {
    id: 1,
    eventType: 'CENTRE_REGISTERED',
    description: 'Centre registered in the system',
    occurredAt: '2024-01-15T09:00:00Z',
    recordedBy: 'officer1',
  },
  {
    id: 2,
    eventType: 'STATUS_CHANGED',
    description: 'Licence status changed from PENDING_RENEWAL to ACTIVE',
    occurredAt: '2024-02-01T14:30:00Z',
    recordedBy: 'admin',
  },
]

describe('CentreLifecycle', () => {
  it('renders empty state when no events', () => {
    render(<CentreLifecycle events={[]} />)
    expect(screen.getByText('No events recorded.')).toBeInTheDocument()
  })

  it('renders all lifecycle events', () => {
    render(<CentreLifecycle events={events} />)
    expect(screen.getByText('Centre Registered')).toBeInTheDocument()
    expect(screen.getByText('Status Changed')).toBeInTheDocument()
  })

  it('shows event descriptions', () => {
    render(<CentreLifecycle events={events} />)
    expect(screen.getByText('Centre registered in the system')).toBeInTheDocument()
  })

  it('shows recorded by', () => {
    render(<CentreLifecycle events={events} />)
    expect(screen.getByText('by officer1')).toBeInTheDocument()
  })
})
