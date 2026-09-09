import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios"
import { clearRefreshToken, getRefreshToken, setRefreshToken } from "@/lib/refresh-token-storage"
import { useAuthStore } from "@/store/auth-store"
import type { ApiResponse } from "@/types/api"
import type { AuthResponse } from "@/types/auth"

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1"

export const api = axios.create({ baseURL })

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * Aynı anda birden fazla istek 401 alırsa (örn. dashboard birkaç query'yi
 * paralel atarken token tam o an süresi dolmuşsa) her biri ayrı ayrı
 * /auth/refresh çağırmasın diye tek bir paylaşılan promise. İlk 401 refresh'i
 * başlatır, diğerleri aynı promise'i bekler.
 */
let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error("No refresh token available")
  }

  const response = await axios.post<ApiResponse<AuthResponse>>(`${baseURL}/auth/refresh`, {
    refreshToken,
  })

  const { accessToken, refreshToken: rotatedRefreshToken } = response.data.data
  useAuthStore.getState().setAccessToken(accessToken)
  setRefreshToken(rotatedRefreshToken)
  return accessToken
}

function handleSessionExpired() {
  useAuthStore.getState().clearSession()
  clearRefreshToken()
  if (!window.location.pathname.startsWith("/login")) {
    window.location.assign("/login")
  }
}

interface RetriableConfig extends InternalAxiosRequestConfig {
  _retried?: boolean
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetriableConfig | undefined
    const status = error.response?.status

    const isAuthEndpoint = originalRequest?.url?.includes("/auth/")
    if (!originalRequest || status !== 401 || originalRequest._retried || isAuthEndpoint) {
      return Promise.reject(error)
    }

    originalRequest._retried = true

    try {
      refreshPromise ??= refreshAccessToken().finally(() => {
        refreshPromise = null
      })
      const newAccessToken = await refreshPromise

      originalRequest.headers.set("Authorization", `Bearer ${newAccessToken}`)
      return api(originalRequest)
    } catch (refreshError) {
      handleSessionExpired()
      return Promise.reject(refreshError)
    }
  },
)

/** Backend her zaman ApiResponse<T> zarfı döner; çağıran taraf sadece T ile ilgilenir. */
export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise
  return response.data.data
}
