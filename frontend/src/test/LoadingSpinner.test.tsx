import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { LoadingSpinner } from '../components/common/LoadingSpinner'

describe('LoadingSpinner', () => {
  it('should render with default message', () => {
    render(<LoadingSpinner />)
    expect(screen.getByText('Loading…')).toBeInTheDocument()
  })

  it('should render with custom message', () => {
    render(<LoadingSpinner message="Fetching centres..." />)
    expect(screen.getByText('Fetching centres...')).toBeInTheDocument()
  })

  it('should render spinner element', () => {
    const { container } = render(<LoadingSpinner />)
    const spinnerDiv = container.querySelector('div[style*="display: flex"]')
    expect(spinnerDiv).toBeInTheDocument()
  })

  it('should have spinner with rotation animation', () => {
    const { container } = render(<LoadingSpinner />)
    const spinner = container.querySelector('div[style*="border-radius"]')
    expect(spinner).toBeInTheDocument()
    expect(spinner).toHaveStyle('animation: spin 0.8s linear infinite')
  })

  it('should have proper spacing and alignment', () => {
    const { container } = render(<LoadingSpinner />)
    const wrapper = container.querySelector('div[style*="display: flex"]')
    expect(wrapper).toHaveStyle('alignItems: center')
    expect(wrapper).toHaveStyle('gap: 12px')
  })
})
