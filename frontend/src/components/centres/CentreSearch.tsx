import type { CentreSearchParams, CentreType, LicenceStatus } from '../../types/centre'

interface Props {
  params: CentreSearchParams
  onUpdate: (p: Partial<CentreSearchParams>) => void
}

const CENTRE_TYPES: { value: CentreType; label: string }[] = [
  { value: 'INFANT_CARE',       label: 'Infant Care' },
  { value: 'STUDENT_CARE',      label: 'Student Care' },
  { value: 'ANCHOR_OPERATOR',   label: 'Anchor Operator' },
  { value: 'PARTNER_OPERATOR',  label: 'Partner Operator' },
]

const STATUSES: { value: LicenceStatus; label: string }[] = [
  { value: 'ACTIVE',          label: 'Active' },
  { value: 'PENDING_RENEWAL', label: 'Pending Renewal' },
  { value: 'SUSPENDED',       label: 'Suspended' },
  { value: 'EXPIRED',         label: 'Expired' },
]

export function CentreSearch({ params, onUpdate }: Props) {
  return (
    <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
      <input
        type="search"
        placeholder="Search by name, licence no, or postal code…"
        value={params.query ?? ''}
        onChange={e => onUpdate({ query: e.target.value || undefined })}
        style={inputStyle}
      />

      <select
        value={params.centreType ?? ''}
        onChange={e => onUpdate({ centreType: (e.target.value as CentreType) || undefined })}
        style={selectStyle}
      >
        <option value="">All Types</option>
        {CENTRE_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
      </select>

      <select
        value={params.licenceStatus ?? ''}
        onChange={e => onUpdate({ licenceStatus: (e.target.value as LicenceStatus) || undefined })}
        style={selectStyle}
      >
        <option value="">All Statuses</option>
        {STATUSES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
      </select>

      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <label style={{ fontSize: 13, color: '#6b7280', whiteSpace: 'nowrap' }}>Renewal due before</label>
        <input
          type="date"
          value={params.renewalDueBefore ?? ''}
          onChange={e => onUpdate({ renewalDueBefore: e.target.value || undefined })}
          style={inputStyle}
        />
      </div>

      <select
        value={`${params.sortBy ?? 'updatedAt'}:${params.sortDir ?? 'desc'}`}
        onChange={e => {
          const [sortBy, sortDir] = e.target.value.split(':')
          onUpdate({ sortBy: sortBy as CentreSearchParams['sortBy'], sortDir: sortDir as 'asc' | 'desc' })
        }}
        style={selectStyle}
      >
        <option value="updatedAt:desc">Last Updated ↓</option>
        <option value="licenceExpiryDate:asc">Expiry (soonest)</option>
        <option value="name:asc">Name A–Z</option>
        <option value="name:desc">Name Z–A</option>
      </select>
    </div>
  )
}

const inputStyle: React.CSSProperties = {
  padding: '8px 12px', borderRadius: 8, border: '1px solid #d1d5db',
  fontSize: 14, minWidth: 240, flex: '1 1 240px',
}

const selectStyle: React.CSSProperties = {
  padding: '8px 12px', borderRadius: 8, border: '1px solid #d1d5db', fontSize: 14,
}
