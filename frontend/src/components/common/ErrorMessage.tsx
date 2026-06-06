interface Props {
  message: string
  onRetry?: () => void
}

export function ErrorMessage({ message, onRetry }: Props) {
  return (
    <div style={{
      padding: '1rem 1.25rem', background: '#fef2f2', border: '1px solid #fecaca',
      borderRadius: 8, color: '#b91c1c', display: 'flex', alignItems: 'center', gap: 12,
    }}>
      <span style={{ fontSize: 18 }}>⚠</span>
      <span style={{ flex: 1 }}>{message}</span>
      {onRetry && (
        <button onClick={onRetry} style={{
          padding: '4px 12px', borderRadius: 6, border: '1px solid #fca5a5',
          background: 'white', color: '#b91c1c', cursor: 'pointer', fontSize: 13,
        }}>
          Retry
        </button>
      )}
    </div>
  )
}
