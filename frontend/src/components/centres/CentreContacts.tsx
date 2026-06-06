import type { CentreContactDto, ContactType } from '../../types/centre'

const CONTACT_TYPE_LABELS: Record<ContactType, string> = {
  PRIMARY: 'Primary Contact',
  HQ_LIAISON: 'HQ Liaison',
  EMERGENCY: 'Emergency Contact',
}

interface Props {
  contacts: CentreContactDto[]
}

export function CentreContacts({ contacts }: Props) {
  const byType = (type: ContactType) => contacts.find(c => c.contactType === type)
  const types: ContactType[] = ['PRIMARY', 'HQ_LIAISON', 'EMERGENCY']

  return (
    <section style={sectionStyle}>
      <h3 style={headingStyle}>Centre Contacts</h3>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1rem' }}>
        {types.map(type => {
          const c = byType(type)
          return (
            <div key={type} style={cardStyle}>
              <span style={typeLabel}>{CONTACT_TYPE_LABELS[type]}</span>
              {c ? (
                <>
                  <p style={{ margin: '6px 0 0', fontWeight: 600, fontSize: 14, color: '#111827' }}>{c.contactName}</p>
                  {c.role && <p style={detailStyle}>{c.role}</p>}
                  {c.email && <p style={detailStyle}><a href={`mailto:${c.email}`} style={{ color: '#6366f1' }}>{c.email}</a></p>}
                  {c.phone && <p style={detailStyle}>{c.phone}</p>}
                </>
              ) : (
                <p style={{ marginTop: 8, color: '#9ca3af', fontSize: 13 }}>Not assigned</p>
              )}
            </div>
          )
        })}
      </div>
    </section>
  )
}

const sectionStyle: React.CSSProperties = { background: 'white', borderRadius: 12, border: '1px solid #e5e7eb', padding: '1.5rem' }
const headingStyle: React.CSSProperties = { margin: 0, marginBottom: 16, fontSize: 16, fontWeight: 600, color: '#111827' }
const cardStyle: React.CSSProperties = { background: '#f9fafb', borderRadius: 8, padding: '1rem', border: '1px solid #e5e7eb' }
const typeLabel: React.CSSProperties = { fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#6b7280' }
const detailStyle: React.CSSProperties = { margin: '3px 0 0', fontSize: 13, color: '#4b5563' }
