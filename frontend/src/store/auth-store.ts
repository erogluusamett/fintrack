import { create } from "zustand"
import type { User } from "@/types/user"

interface AuthState {
  user: User | null
  accessToken: string | null
  /** Uygulama ilk açıldığında refresh token ile sessiz giriş denemesi sürüyor mu. */
  isBootstrapping: boolean
  setSession: (user: User, accessToken: string) => void
  setAccessToken: (accessToken: string) => void
  setUser: (user: User) => void
  clearSession: () => void
  finishBootstrapping: () => void
}

/**
 * accessToken burada — sadece bellekte, persist edilmeden — tutulur.
 * Axios interceptor'ı (api/axios.ts) buradan okur; React dışında da
 * `useAuthStore.getState()` ile erişilebilir olması bunun için Zustand
 * seçilme sebeplerinden biri (React Context ile bu kolay olmazdı).
 */
export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  isBootstrapping: true,
  setSession: (user, accessToken) => set({ user, accessToken }),
  setAccessToken: (accessToken) => set({ accessToken }),
  setUser: (user) => set({ user }),
  clearSession: () => set({ user: null, accessToken: null }),
  finishBootstrapping: () => set({ isBootstrapping: false }),
}))

export function isAuthenticated(): boolean {
  return useAuthStore.getState().accessToken !== null
}
