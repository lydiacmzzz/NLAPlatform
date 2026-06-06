export function LoadingSpinner({ message = 'Loading…' }: { message?: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '2rem', color: '#6b7280' }}>
      <div style={{
        width: 24, height: 24, borderRadius: '50%',
        border: '3px solid #e5e7eb', borderTopColor: '#3b82f6',
        animation: 'spin 0.8s linear infinite',
      }} />
      <span>{message}</span>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  )
}
