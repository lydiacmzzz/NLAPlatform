import { format } from 'date-fns'
import type { CentreProfileDto, LicenceStatus } from '../../types/centre'
import { LicenceStatusBadge } from './LicenceStatusBadge'
import { InlineEdit } from '../common/InlineEdit'

const CENTRE_TYPE_LABELS: Record<string, string> = {
  INFANT_CARE: 'Infant Care', STUDENT_CARE: 'Student Care',
  ANCHOR_OPERATOR: 'Anchor Operator', PARTNER_OPERATOR: 'Partner Operator',
}

const STATUS_OPTIONS = [
  { value: 'ACTIVE',          label: 'Active' },
  { value: 'PENDING_RENEWAL', label: 'Pending Renewal' },
  { value: 'SUSPENDED',       label: 'Suspended' },
  { value: 'EXPIRED',         label: 'Expired' },
]

interface Props {
  centre: CentreProfileDto
  canEdit: boolean
  onFieldSave: (field: string, value: string) => Promise<void>
}

export function CentreProfile({ centre, canEdit, onFieldSave }: Props) {
  return (
    <section style={sectionStyle}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20, gap: 12 }}>
        <div>
          <h2 style={{ margin: 0, fontSize: 22, fontWeight: 700, color: '#111827' }}>
            <InlineEdit
              value={centre.name}
              canEdit={canEdit}
              onSave={v => onFieldSave('name', v)}
              validate={v => v.trim().length < 2 ? 'Name must be at least 2 characters' : null}
            />
          </h2>
          <div style={{ marginTop: 6, display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
            <code style={{ fontSize: 12, background: '#f3f4f6', padding: '2px 8px', borderRadius: 4 }}>
              {centre.centreId}
            </code>
            <span style={{ fontSize: 13, color: '#6b7280' }}>Licence: {centre.licenceNumber}</span>
            <span style={{ fontSize: 13, color: '#6b7280' }}>{CENTRE_TYPE_LABELS[centre.centreType]}</span>
          </div>
        </div>
        <div>
          {canEdit ? (
            <InlineEdit
              value={centre.licenceStatus}
              canEdit={canEdit}
              onSave={v => onFieldSave('licenceStatus', v)}
              type="select"
              options={STATUS_OPTIONS}
            />
          ) : (
            <LicenceStatusBadge status={centre.licenceStatus} />
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1.25rem' }}>
        <Field label="Address">
          <InlineEdit value={centre.address} canEdit={canEdit} onSave={v => onFieldSave('address', v)} />
        </Field>
        <Field label="Postal Code">
          <InlineEdit
            value={centre.postalCode} canEdit={canEdit}
            onSave={v => onFieldSave('postalCode', v)}
            validate={v => /^\d{6}$/.test(v) ? null : 'Must be a 6-digit postal code'}
          />
        </Field>
        <Field label="Operating Hours">
          <InlineEdit value={centre.operatingHours ?? ''} canEdit={canEdit} onSave={v => onFieldSave('operatingHours', v)} placeholder="Not set" />
        </Field>
        <Field label="Capacity">
          <InlineEdit
            value={String(centre.capacity)} canEdit={canEdit} type="number"
            onSave={v => onFieldSave('capacity', v)}
            validate={v => Number(v) > 0 ? null : 'Must be positive'}
          />
        </Field>
        <Field label="Licence Issue Date">
          <InlineEdit value={centre.licenceIssueDate ?? ''} canEdit={canEdit} type="date" onSave={v => onFieldSave('licenceIssueDate', v)} placeholder="Not set" />
        </Field>
        <Field label="Licence Expiry Date">
          <InlineEdit value={centre.licenceExpiryDate ?? ''} canEdit={canEdit} type="date" onSave={v => onFieldSave('licenceExpiryDate', v)} placeholder="Not set" />
        </Field>
        <Field label="Renewal Due Date">
          <InlineEdit value={centre.renewalDueDate ?? ''} canEdit={canEdit} type="date" onSave={v => onFieldSave('renewalDueDate', v)} placeholder="Not set" />
        </Field>
        <Field label="Application Stage">
          <InlineEdit value={centre.applicationStage ?? ''} canEdit={canEdit} onSave={v => onFieldSave('applicationStage', v)} placeholder="Not set" />
        </Field>
      </div>

      <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #f3f4f6', fontSize: 12, color: '#9ca3af' }}>
        Last updated {format(new Date(centre.updatedAt), 'dd MMM yyyy HH:mm')}
        {centre.updatedBy && ` by ${centre.updatedBy}`}
      </div>
    </section>
  )
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <span style={{ fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#6b7280' }}>
        {label}
      </span>
      <div style={{ marginTop: 4, fontSize: 14, color: '#111827' }}>{children}</div>
    </div>
  )
}

const sectionStyle: React.CSSProperties = { background: 'white', borderRadius: 12, border: '1px solid #e5e7eb', padding: '1.5rem' }
