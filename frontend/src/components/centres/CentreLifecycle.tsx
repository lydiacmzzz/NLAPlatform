import { format } from 'date-fns'
import type { LifecycleEventDto } from '../../types/centre'

const EVENT_COLORS: Record<string, string> = {
  CENTRE_REGISTERED:  '#6366f1',
  STATUS_CHANGED:     '#f59e0b',
  KAH_APPOINTED:      '#10b981',
  KAH_CHANGE_PENDING: '#f97316',
  PROFILE_UPDATED:    '#3b82f6',
}

interface Props {
  events: LifecycleEventDto[]
}

export function CentreLifecycle({ events }: Props) {
  return (
    <section style={sectionStyle}>
      <h3 style={headingStyle}>Lifecycle Timeline</h3>
      {events.length === 0 ? (
        <p style={{ color: '#9ca3af', fontSize: 14 }}>No events recorded.</p>
      ) : (
        <div style={{ position: 'relative', paddingLeft: 28 }}>
          <div style={{
            position: 'absolute', left: 10, top: 8, bottom: 8,
            width: 2, background: '#e5e7eb', borderRadius: 1,
          }} />
          {events.map((ev, i) => (
            <div key={ev.id} style={{ position: 'relative', marginBottom: i < events.length - 1 ? 20 : 0 }}>
              <div style={{
                position: 'absolute', left: -22, top: 4, width: 12, height: 12,
                borderRadius: '50%', background: EVENT_COLORS[ev.eventType] ?? '#9ca3af',
                border: '2px solid white', boxShadow: '0 0 0 2px #e5e7eb',
              }} />
              <div>
                <div style={{ display: 'flex', gap: 8, alignItems: 'baseline' }}>
                  <span style={{ fontSize: 13, fontWeight: 600, color: '#111827' }}>
                    {formatEventType(ev.eventType)}
                  </span>
                  <span style={{ fontSize: 11, color: '#9ca3af' }}>
                    {format(new Date(ev.occurredAt), 'dd MMM yyyy HH:mm')}
                  </span>
                </div>
                <p style={{ margin: '2px 0 0', fontSize: 13, color: '#4b5563' }}>{ev.description}</p>
                <p style={{ margin: '2px 0 0', fontSize: 11, color: '#9ca3af' }}>by {ev.recordedBy}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

function formatEventType(type: string): string {
  return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase())
}

const sectionStyle: React.CSSProperties = { background: 'white', borderRadius: 12, border: '1px solid #e5e7eb', padding: '1.5rem' }
const headingStyle: React.CSSProperties = { margin: 0, marginBottom: 20, fontSize: 16, fontWeight: 600, color: '#111827' }
