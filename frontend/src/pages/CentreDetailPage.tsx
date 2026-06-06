import { useParams, useNavigate } from 'react-router-dom'
import { useCentre } from '../hooks/useCentre'
import { useWaiverHistory } from '../hooks/useWaiverHistory'
import { useAuth } from '../contexts/AuthContext'
import { CentreProfile } from '../components/centres/CentreProfile'
import { KAHDetails } from '../components/centres/KAHDetails'
import { CentreContacts } from '../components/centres/CentreContacts'
import { CentreLifecycle } from '../components/centres/CentreLifecycle'
import { WaiverHistory } from '../components/centres/WaiverHistory'
import { LoadingSpinner } from '../components/common/LoadingSpinner'
import { ErrorMessage } from '../components/common/ErrorMessage'

export function CentreDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { canEdit, canViewWaivers } = useAuth()
  const { centre, loading, error, saving, update, reload } = useCentre(Number(id))
  const { waivers, loading: waiversLoading } = useWaiverHistory(Number(id), canViewWaivers)

  const handleFieldSave = async (field: string, value: string) => {
    await update({ [field]: field === 'capacity' ? Number(value) : value || undefined })
  }

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '2rem 1.5rem' }}>
      <button
        onClick={() => navigate(-1)}
        style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280', fontSize: 14, marginBottom: 16, padding: 0 }}
      >
        ← Back to Centres
      </button>

      {saving && (
        <div style={{ padding: '8px 16px', background: '#eff6ff', borderRadius: 8, marginBottom: 12, fontSize: 13, color: '#1d4ed8' }}>
          Saving…
        </div>
      )}

      {error && <ErrorMessage message={error} onRetry={reload} />}
      {loading && <LoadingSpinner message="Loading centre profile…" />}

      {!loading && centre && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <CentreProfile centre={centre} canEdit={canEdit} onFieldSave={handleFieldSave} />
          <KAHDetails kah={centre.currentKah} canEdit={canEdit} />
          <CentreContacts contacts={centre.contacts} />
          <CentreLifecycle events={centre.lifecycleEvents} />
          {canViewWaivers && <WaiverHistory waivers={waivers} loading={waiversLoading} />}
        </div>
      )}
    </div>
  )
}
