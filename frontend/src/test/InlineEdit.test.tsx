import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { InlineEdit } from '../components/common/InlineEdit'

describe('InlineEdit', () => {
  it('shows display value when not editing', () => {
    render(<InlineEdit value="Happy Kids" onSave={vi.fn()} canEdit={true} />)
    expect(screen.getByText('Happy Kids')).toBeInTheDocument()
    expect(screen.queryByRole('textbox')).toBeNull()
  })

  it('does not enter edit mode when canEdit is false', async () => {
    render(<InlineEdit value="No Edit" onSave={vi.fn()} canEdit={false} />)
    await userEvent.click(screen.getByText('No Edit'))
    expect(screen.queryByRole('textbox')).toBeNull()
  })

  it('enters edit mode on click and shows input', async () => {
    render(<InlineEdit value="Editable" onSave={vi.fn()} canEdit={true} />)
    await userEvent.click(screen.getByText('Editable'))
    expect(screen.getByRole('textbox')).toBeInTheDocument()
  })

  it('calls onSave with new value on ✓ click', async () => {
    const onSave = vi.fn().mockResolvedValue(undefined)
    render(<InlineEdit value="Old" onSave={onSave} canEdit={true} />)
    await userEvent.click(screen.getByText('Old'))
    await userEvent.clear(screen.getByRole('textbox'))
    await userEvent.type(screen.getByRole('textbox'), 'New Value')
    await userEvent.click(screen.getByText('✓'))
    await waitFor(() => expect(onSave).toHaveBeenCalledWith('New Value'))
  })

  it('cancels on ✕ click and reverts value', async () => {
    const onSave = vi.fn()
    render(<InlineEdit value="Original" onSave={onSave} canEdit={true} />)
    await userEvent.click(screen.getByText('Original'))
    await userEvent.clear(screen.getByRole('textbox'))
    await userEvent.type(screen.getByRole('textbox'), 'Changed')
    await userEvent.click(screen.getByText('✕'))
    expect(screen.getByText('Original')).toBeInTheDocument()
    expect(onSave).not.toHaveBeenCalled()
  })

  it('shows validation error and does not save', async () => {
    const onSave = vi.fn()
    render(<InlineEdit value="abc" onSave={onSave} canEdit={true} validate={v => v.length < 5 ? 'Too short' : null} />)
    await userEvent.click(screen.getByText('abc'))
    await userEvent.click(screen.getByText('✓'))
    expect(screen.getByText('Too short')).toBeInTheDocument()
    expect(onSave).not.toHaveBeenCalled()
  })
})
