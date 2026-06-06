import { useCentreSearch } from '../hooks/useCentreSearch'
import { CentreSearch } from '../components/centres/CentreSearch'
import { CentreList } from '../components/centres/CentreList'
import { LoadingSpinner } from '../components/common/LoadingSpinner'
import { ErrorMessage } from '../components/common/ErrorMessage'

export function CentreListPage() {
  const { result, loading, error, params, updateParams, goToPage } = useCentreSearch()

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ margin: 0, fontSize: 24, fontWeight: 700, color: '#111827' }}>Centres</h1>
        <p style={{ margin: '4px 0 0', color: '#6b7280', fontSize: 14 }}>
          Search and manage licensed childcare centres
        </p>
      </div>

      <CentreSearch params={params} onUpdate={updateParams} />

      {error && <ErrorMessage message={error} />}
      {loading && <LoadingSpinner message="Loading centres…" />}
      {!loading && !error && result && (
        <CentreList result={result} onPageChange={goToPage} />
      )}
    </div>
  )
}
