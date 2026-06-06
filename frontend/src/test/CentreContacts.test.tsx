import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { CentreContacts } from '../components/centres/CentreContacts'
import type { CentreContactDto } from '../types/centre'

const contacts: CentreContactDto[] = [
  { id: 1, contactType: 'PRIMARY', contactName: 'Alice Tan', role: 'Admin Manager', email: 'alice@centre.sg', phone: '91234567' },
  { id: 2, contactType: 'EMERGENCY', contactName: 'Bob Lim', role: null, email: null, phone: '98765432' },
]

describe('CentreContacts', () => {
  it('renders all three contact type sections', () => {
    render(<CentreContacts contacts={contacts} />)
    expect(screen.getByText('Primary Contact')).toBeInTheDocument()
    expect(screen.getByText('HQ Liaison')).toBeInTheDocument()
    expect(screen.getByText('Emergency Contact')).toBeInTheDocument()
  })

  it('shows not assigned for missing contact type', () => {
    render(<CentreContacts contacts={contacts} />)
    expect(screen.getByText('Not assigned')).toBeInTheDocument()
  })

  it('renders primary contact details', () => {
    render(<CentreContacts contacts={contacts} />)
    expect(screen.getByText('Alice Tan')).toBeInTheDocument()
    expect(screen.getByText('Admin Manager')).toBeInTheDocument()
    expect(screen.getByText('alice@centre.sg')).toBeInTheDocument()
  })
})
