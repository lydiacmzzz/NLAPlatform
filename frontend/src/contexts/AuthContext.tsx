import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { authService } from '../services/authService'
import type { AuthUser, UserRole } from '../types/centre'

interface AuthContextValue {
  user: AuthUser | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  canEdit: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

const EDIT_ROLES: UserRole[] = ['ECDA_OFFICER', 'HQ_ADMIN']

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => authService.getStoredUser())

  const login = useCallback(async (username: string, password: string) => {
    const u = await authService.login(username, password)
    setUser(u)
  }, [])

  const logout = useCallback(() => {
    authService.logout()
    setUser(null)
  }, [])

  const canEdit = user !== null && EDIT_ROLES.includes(user.role)

  return (
    <AuthContext.Provider value={{ user, login, logout, canEdit }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
