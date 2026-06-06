import { format } from 'date-fns'
import type { KahDetailDto } from '../../types/centre'

interface Props {
  kah: KahDetailDto | null
  canEdit: boolean
}

export function KAHDetails({ kah, canEdit }: Props) {
  if (!kah) {
    return (
      <section style={sectionStyle}>
        <h3 style={headingStyle}>Key Appointment Holder</h3>
        <p style={{ color: '#9ca3af', fontSize: 14 }}>No KAH currently appointed.</p>
      </section>
    )
  }

  return (
    <section style={sectionStyle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h3 style={headingStyle}>Key Appointment Holder</h3>
        {kah.pendingApproval && (
          <span style={{
            padding: '3px 10px', borderRadius: 20, background: '#fff7ed',
            color: '#c2410c', fontSize: 12, fontWeight: 500, border: '1px solid #fed7aa',
          }}>
            ⏳ Change Pending Approval
          </span>
        )}
      </div>

      <div style={gridStyle}>
        <Field label="Principal Name" value={kah.principalName} />
        <Field label="NRIC" value={kah.nric} />
        <Field label="Email" value={kah.email} />
        <Field label="Phone" value={kah.phone} />
        <Field label="Appointment Start" value={format(new Date(kah.appointmentStartDate), 'dd MMM yyyy')} />
        {kah.appointmentEndDate && (
          <Field label="Appointment End" value={format(new Date(kah.appointmentEndDate), 'dd MMM yyyy')} />
        )}
      </div>

      {kah.licenceConditions && (
        <div style={{ marginTop: 16 }}>
          <span style={labelStyle}>Licence Conditions</span>
          <p style={{ marginTop: 4, fontSize: 14, color: '#374151', whiteSpace: 'pre-wrap' }}>
            {kah.licenceConditions}
          </p>
        </div>
      )}

      {canEdit && (
        <div style={{ marginTop: 16 }}>
          <button style={linkBtnStyle}>+ Appoint New KAH</button>
        </div>
      )}
    </section>
  )
}

function Field({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <div>
      <span style={labelStyle}>{label}</span>
      <p style={{ marginTop: 2, fontSize: 14, fontWeight: 500, color: value ? '#111827' : '#9ca3af' }}>
        {value ?? '—'}
      </p>
    </div>
  )
}

const sectionStyle: React.CSSProperties = {
  background: 'white', borderRadius: 12, border: '1px solid #e5e7eb', padding: '1.5rem',
}
const headingStyle: React.CSSProperties = {
  margin: 0, marginBottom: 16, fontSize: 16, fontWeight: 600, color: '#111827',
}
const gridStyle: React.CSSProperties = {
  display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '1rem',
}
const labelStyle: React.CSSProperties = { fontSize: 12, color: '#6b7280', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.04em' }
const linkBtnStyle: React.CSSProperties = {
  padding: '6px 12px', borderRadius: 6, border: '1px solid #6366f1', background: 'white',
  color: '#6366f1', cursor: 'pointer', fontSize: 13, fontWeight: 500,
}
