import { useState, useRef, useEffect } from 'react'

interface Props {
  value: string
  onSave: (value: string) => Promise<void> | void
  validate?: (value: string) => string | null
  canEdit: boolean
  type?: 'text' | 'number' | 'date' | 'select'
  options?: { label: string; value: string }[]
  placeholder?: string
}

export function InlineEdit({ value, onSave, validate, canEdit, type = 'text', options, placeholder }: Props) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState(value)
  const [saving, setSaving] = useState(false)
  const [fieldError, setFieldError] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement & HTMLSelectElement>(null)

  useEffect(() => {
    if (editing) inputRef.current?.focus()
  }, [editing])

  useEffect(() => {
    if (!editing) setDraft(value)
  }, [value, editing])

  const handleSave = async () => {
    const err = validate?.(draft) ?? null
    if (err) { setFieldError(err); return }
    if (draft === value) { setEditing(false); return }
    setSaving(true)
    setFieldError(null)
    try {
      await onSave(draft)
      setEditing(false)
    } catch {
      setFieldError('Failed to save.')
    } finally {
      setSaving(false)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSave()
    if (e.key === 'Escape') { setEditing(false); setDraft(value); setFieldError(null) }
  }

  if (!editing) {
    return (
      <span
        onClick={() => canEdit && setEditing(true)}
        style={{
          padding: '2px 4px', borderRadius: 4, minWidth: 60, display: 'inline-block',
          cursor: canEdit ? 'text' : 'default',
          border: canEdit ? '1px dashed transparent' : 'none',
          transition: 'border-color 0.15s',
        }}
        onMouseEnter={e => { if (canEdit) (e.currentTarget.style.borderColor = '#d1d5db') }}
        onMouseLeave={e => { (e.currentTarget.style.borderColor = 'transparent') }}
      >
        {value || <span style={{ color: '#9ca3af' }}>{placeholder ?? '—'}</span>}
      </span>
    )
  }

  const inputStyle: React.CSSProperties = {
    padding: '4px 8px', borderRadius: 6, border: `1px solid ${fieldError ? '#f87171' : '#6366f1'}`,
    fontSize: 'inherit', outline: 'none', minWidth: 120,
  }

  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
      {type === 'select' && options ? (
        <select
          ref={inputRef as React.Ref<HTMLSelectElement>}
          value={draft}
          onChange={e => setDraft(e.target.value)}
          onKeyDown={handleKeyDown}
          style={inputStyle}
          disabled={saving}
        >
          {options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      ) : (
        <input
          ref={inputRef as React.Ref<HTMLInputElement>}
          type={type}
          value={draft}
          onChange={e => setDraft(e.target.value)}
          onKeyDown={handleKeyDown}
          style={inputStyle}
          disabled={saving}
        />
      )}
      <button onClick={handleSave} disabled={saving} style={{ ...btnStyle, background: '#6366f1', color: 'white' }}>
        {saving ? '…' : '✓'}
      </button>
      <button onClick={() => { setEditing(false); setDraft(value); setFieldError(null) }} disabled={saving} style={btnStyle}>
        ✕
      </button>
      {fieldError && <span style={{ color: '#ef4444', fontSize: 12 }}>{fieldError}</span>}
    </span>
  )
}

const btnStyle: React.CSSProperties = {
  padding: '3px 8px', borderRadius: 4, border: '1px solid #d1d5db',
  cursor: 'pointer', fontSize: 13, background: 'white',
}
