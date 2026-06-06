import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { ErrorMessage } from '../components/common/ErrorMessage'

describe('ErrorMessage', () => {
  it('should render error message', () => {
    render(<ErrorMessage message="Something went wrong" />)
    expect(screen.getByText('Something went wrong')).toBeInTheDocument()
  })

  it('should display warning icon', () => {
    const { container } = render(<ErrorMessage message="Error occurred" />)
    expect(container.textContent).toContain('⚠')
  })

  it('should not show retry button when onRetry is not provided', () => {
    render(<ErrorMessage message="Error" />)
    expect(screen.queryByRole('button', { name: /retry/i })).not.toBeInTheDocument()
  })

  it('should show retry button when onRetry is provided', () => {
    render(<ErrorMessage message="Error" onRetry={vi.fn()} />)
    expect(screen.getByRole('button', { name: /retry/i })).toBeInTheDocument()
  })

  it('should call onRetry when retry button is clicked', async () => {
    const onRetry = vi.fn()
    const { user } = render(<ErrorMessage message="Error" onRetry={onRetry} />)

    await user.click(screen.getByRole('button', { name: /retry/i }))

    expect(onRetry).toHaveBeenCalledOnce()
  })

  it('should have proper error styling', () => {
    const { container } = render(<ErrorMessage message="Error" />)
    const wrapper = container.firstChild as HTMLElement
    expect(wrapper).toHaveStyle('background: #fef2f2')
    expect(wrapper).toHaveStyle('color: #b91c1c')
    expect(wrapper).toHaveStyle('border: 1px solid #fecaca')
  })

  it('should render different error messages', () => {
    const { rerender } = render(<ErrorMessage message="First error" />)
    expect(screen.getByText('First error')).toBeInTheDocument()

    rerender(<ErrorMessage message="Second error" />)
    expect(screen.getByText('Second error')).toBeInTheDocument()
    expect(screen.queryByText('First error')).not.toBeInTheDocument()
  })
})
