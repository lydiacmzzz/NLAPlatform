import type { LicenceStatus } from '../../types/centre'

const CONFIG: Record<LicenceStatus, { label: string; bg: string; color: string; dot: string }> = {
  ACTIVE:          { label: 'Active',          bg: '#dcfce7', color: '#166534', dot: '#16a34a' },
  PENDING_RENEWAL: { label: 'Pending Renewal', bg: '#fef9c3', color: '#854d0e', dot: '#ca8a04' },
  SUSPENDED:       { label: 'Suspended',       bg: '#fde8d8', color: '#9a3412', dot: '#ea580c' },
  EXPIRED:         { label: 'Expired',         bg: '#f3f4f6', color: '#374151', dot: '#9ca3af' },
}

export function LicenceStatusBadge({ status }: { status: LicenceStatus }) {
  const { label, bg, color, dot } = CONFIG[status]
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      padding: '3px 10px', borderRadius: 20, background: bg, color,
      fontSize: 13, fontWeight: 500,
    }}>
      <span style={{ width: 7, height: 7, borderRadius: '50%', background: dot, display: 'inline-block' }} />
      {label}
    </span>
  )
}
