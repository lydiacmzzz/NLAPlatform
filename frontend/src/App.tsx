import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import { LoginPage } from './pages/LoginPage'
import { CentreListPage } from './pages/CentreListPage'
import { CentreDetailPage } from './pages/CentreDetailPage'

function NavBar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  return (
    <nav style={{
      height: 56, background: '#1e1b4b', display: 'flex', alignItems: 'center',
      padding: '0 1.5rem', gap: 16,
    }}>
      <span
        onClick={() => navigate('/')}
        style={{ color: 'white', fontWeight: 700, fontSize: 15, cursor: 'pointer', flex: 1 }}
      >
        ECDA Regulatory & Licensing
      </span>
      {user && (
        <>
          <span style={{ color: '#a5b4fc', fontSize: 13 }}>
            {user.fullName ?? user.username} · {user.role.replace('_', ' ')}
          </span>
          <button
            onClick={() => { logout(); navigate('/login') }}
            style={{
              padding: '5px 14px', borderRadius: 6, border: '1px solid #4f46e5',
              background: 'transparent', color: '#c7d2fe', cursor: 'pointer', fontSize: 13,
            }}
          >
            Sign Out
          </button>
        </>
      )}
    </nav>
  )
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  return user ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div style={{ minHeight: '100vh', background: '#f9fafb', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
          <NavBar />
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={<ProtectedRoute><CentreListPage /></ProtectedRoute>} />
            <Route path="/centres/:id" element={<ProtectedRoute><CentreDetailPage /></ProtectedRoute>} />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}
