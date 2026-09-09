import { useMutation } from "@tanstack/react-query"
import { useEffect } from "react"
import { authApi } from "@/api/auth.api"
import { userApi } from "@/api/user.api"
import { clearRefreshToken, getRefreshToken, setRefreshToken } from "@/lib/refresh-token-storage"
import { useAuthStore } from "@/store/auth-store"
import type { LoginRequest, RegisterRequest } from "@/types/auth"

async function establishSession(accessToken: string, refreshToken: string) {
  useAuthStore.getState().setAccessToken(accessToken)
  setRefreshToken(refreshToken)
  const user = await userApi.getMe()
  useAuthStore.getState().setSession(user, accessToken)
}

export function useLogin() {
  return useMutation({
    mutationFn: async (credentials: LoginRequest) => {
      const { accessToken, refreshToken } = await authApi.login(credentials)
      await establishSession(accessToken, refreshToken)
    },
  })
}

export function useRegister() {
  return useMutation({
    mutationFn: async (payload: RegisterRequest) => {
      const { accessToken, refreshToken } = await authApi.register(payload)
      await establishSession(accessToken, refreshToken)
    },
  })
}

export function useLogout() {
  return useMutation({
    mutationFn: async () => {
      const refreshToken = getRefreshToken()
      if (refreshToken) {
        // En iyi çaba: sunucu tarafında refresh token'ı iptal et. Bu başarısız
        // olsa bile kullanıcıyı client tarafında çıkış yaptırmaktan vazgeçmiyoruz.
        await authApi.logout(refreshToken).catch(() => undefined)
      }
    },
    onSettled: () => {
      useAuthStore.getState().clearSession()
      clearRefreshToken()
    },
  })
}

/**
 * Uygulama ilk açıldığında access token bellekte yoktur (sayfa yenilendi
 * demektir) ama localStorage'da bir refresh token olabilir — varsa sessizce
 * yeni bir access token alıp oturumu geri kurar. App.tsx bunu bir kez,
 * root seviyede çalıştırır.
 */
export function useBootstrapAuth() {
  useEffect(() => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      useAuthStore.getState().finishBootstrapping()
      return
    }

    authApi
      .refresh(refreshToken)
      .then(({ accessToken, refreshToken: rotated }) => establishSession(accessToken, rotated))
      .catch(() => {
        useAuthStore.getState().clearSession()
        clearRefreshToken()
      })
      .finally(() => {
        useAuthStore.getState().finishBootstrapping()
      })
  }, [])
}
