import { useNavigate } from 'react-router-dom'
import { format } from 'date-fns'
import type { CentreSummaryDto, PagedResponse } from '../../types/centre'
import { LicenceStatusBadge } from './LicenceStatusBadge'

const CENTRE_TYPE_LABELS: Record<string, string> = {
  INFANT_CARE: 'Infant Care', STUDENT_CARE: 'Student Care',
  ANCHOR_OPERATOR: 'Anchor Operator', PARTNER_OPERATOR: 'Partner Operator',
}

interface Props {
  result: PagedResponse<CentreSummaryDto>
  onPageChange: (page: number) => void
}

export function CentreList({ result, onPageChange }: Props) {
  const navigate = useNavigate()

  if (result.content.length === 0) {
    return <p style={{ color: '#6b7280', padding: '2rem 0' }}>No centres found.</p>
  }

  return (
    <div>
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
          <thead>
            <tr style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
              {['Centre ID', 'Name', 'Type', 'Postal', 'Licence Status', 'Expiry', 'Last Updated'].map(h => (
                <th key={h} style={thStyle}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {result.content.map(c => (
              <tr
                key={c.id}
                onClick={() => navigate(`/centres/${c.id}`)}
                style={{ borderBottom: '1px solid #f3f4f6', cursor: 'pointer', transition: 'background 0.1s' }}
                onMouseEnter={e => (e.currentTarget.style.background = '#f9fafb')}
                onMouseLeave={e => (e.currentTarget.style.background = 'white')}
              >
                <td style={tdStyle}><code style={{ fontSize: 12 }}>{c.centreId}</code></td>
                <td style={{ ...tdStyle, fontWeight: 500 }}>{c.name}</td>
                <td style={tdStyle}>{CENTRE_TYPE_LABELS[c.centreType]}</td>
                <td style={tdStyle}>{c.postalCode}</td>
                <td style={tdStyle}><LicenceStatusBadge status={c.licenceStatus} /></td>
                <td style={tdStyle}>
                  {c.licenceExpiryDate ? format(new Date(c.licenceExpiryDate), 'dd MMM yyyy') : '—'}
                </td>
                <td style={{ ...tdStyle, color: '#6b7280', fontSize: 12 }}>
                  {format(new Date(c.updatedAt), 'dd MMM yyyy HH:mm')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16, fontSize: 14, color: '#6b7280' }}>
        <span>
          {result.totalElements} result{result.totalElements !== 1 ? 's' : ''}
          {result.totalPages > 1 && ` · Page ${result.page + 1} of ${result.totalPages}`}
        </span>
        {result.totalPages > 1 && (
          <div style={{ display: 'flex', gap: 6 }}>
            <PagBtn label="← Prev" disabled={result.page === 0} onClick={() => onPageChange(result.page - 1)} />
            <PagBtn label="Next →" disabled={result.page >= result.totalPages - 1} onClick={() => onPageChange(result.page + 1)} />
          </div>
        )}
      </div>
    </div>
  )
}

function PagBtn({ label, disabled, onClick }: { label: string; disabled: boolean; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        padding: '6px 14px', borderRadius: 6, border: '1px solid #d1d5db',
        cursor: disabled ? 'default' : 'pointer',
        background: 'white', color: disabled ? '#9ca3af' : '#374151', fontSize: 13,
      }}
    >
      {label}
    </button>
  )
}

const thStyle: React.CSSProperties = { padding: '10px 14px', textAlign: 'left', fontWeight: 600, color: '#374151', whiteSpace: 'nowrap' }
const tdStyle: React.CSSProperties = { padding: '12px 14px', verticalAlign: 'middle' }
