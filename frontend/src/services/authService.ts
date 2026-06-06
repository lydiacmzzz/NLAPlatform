import api from './api'
import type { AuthUser } from '../types/centre'

export const authService = {
  async login(username: string, password: string): Promise<AuthUser> {
    const { data } = await api.post('/auth/login', { username, password })
    const user: AuthUser = {
      username: data.username,
      role: data.role,
      fullName: data.fullName,
      token: data.token,
    }
    localStorage.setItem('ecda_token', data.token)
    localStorage.setItem('ecda_user', JSON.stringify(user))
    return user
  },

  logout() {
    localStorage.removeItem('ecda_token')
    localStorage.removeItem('ecda_user')
  },

  getStoredUser(): AuthUser | null {
    const raw = localStorage.getItem('ecda_user')
    return raw ? (JSON.parse(raw) as AuthUser) : null
  },
}
