import { format } from 'date-fns'
import type { WaiverHistoryDto, WaiverStatus } from '../../types/centre'

const STATUS_COLORS: Record<WaiverStatus, { bg: string; text: string }> = {
  APPROVED:   { bg: '#dcfce7', text: '#166534' },
  EXPIRED:    { bg: '#f3f4f6', text: '#6b7280' },
  SUPERSEDED: { bg: '#fef3c7', text: '#92400e' },
  REJECTED:   { bg: '#fee2e2', text: '#991b1b' },
}

interface Props {
  waivers: WaiverHistoryDto[]
  loading?: boolean
}

export function WaiverHistory({ waivers, loading }: Props) {
  return (
    <section style={sectionStyle}>
      <h3 style={headingStyle}>Waiver History</h3>

      {loading && (
        <p style={{ color: '#9ca3af', fontSize: 14 }}>Loading waiver history…</p>
      )}

      {!loading && waivers.length === 0 && (
        <p style={{ color: '#9ca3af', fontSize: 14 }}>No waiver history found for this centre.</p>
      )}

      {!loading && waivers.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {waivers.map(w => (
            <WaiverCard key={w.id} waiver={w} />
          ))}
        </div>
      )}
    </section>
  )
}

function WaiverCard({ waiver }: { waiver: WaiverHistoryDto }) {
  const statusColors = STATUS_COLORS[waiver.waiverStatus]
  return (
    <div style={cardStyle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
        <div>
          <span style={{ fontSize: 13, fontWeight: 600, color: '#111827' }}>{waiver.waiverTitle}</span>
          <span style={{ marginLeft: 10, fontSize: 12, color: '#6b7280' }}>{waiver.waiverType}</span>
        </div>
        <span style={{
          fontSize: 11, fontWeight: 600, padding: '2px 8px', borderRadius: 9999,
          background: statusColors.bg, color: statusColors.text,
        }}>
          {waiver.waiverStatus}
        </span>
      </div>

      <div style={{ marginTop: 10, display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: '0.5rem 1.5rem' }}>
        {waiver.approvalDate && (
          <Field label="Approval Date">{format(new Date(waiver.approvalDate), 'dd MMM yyyy')}</Field>
        )}
        {waiver.expiryDate && (
          <Field label="Expiry Date">{format(new Date(waiver.expiryDate), 'dd MMM yyyy')}</Field>
        )}
        {waiver.approvedBy && (
          <Field label="Approved By">{waiver.approvedBy}</Field>
        )}
      </div>

      {waiver.officerRemarks && (
        <div style={{ marginTop: 8 }}>
          <Field label="Officer Remarks">{waiver.officerRemarks}</Field>
        </div>
      )}

      {waiver.supportingDocumentName && waiver.supportingDocumentUrl && (
        <div style={{ marginTop: 8 }}>
          <Field label="Supporting Document">
            <a
              href={waiver.supportingDocumentUrl}
              target="_blank"
              rel="noopener noreferrer"
              style={{ color: '#2563eb', fontSize: 13, textDecoration: 'none' }}
            >
              {waiver.supportingDocumentName}
            </a>
          </Field>
        </div>
      )}
    </div>
  )
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <span style={{ fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#6b7280' }}>
        {label}
      </span>
      <div style={{ marginTop: 2, fontSize: 13, color: '#374151' }}>{children}</div>
    </div>
  )
}

const sectionStyle: React.CSSProperties = { background: 'white', borderRadius: 12, border: '1px solid #e5e7eb', padding: '1.5rem' }
const headingStyle: React.CSSProperties = { margin: 0, marginBottom: 20, fontSize: 16, fontWeight: 600, color: '#111827' }
const cardStyle: React.CSSProperties = { padding: '1rem', borderRadius: 8, border: '1px solid #e5e7eb', background: '#fafafa' }
