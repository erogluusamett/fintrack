import { api, unwrap } from "./axios"
import type {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
  VerifyEmailRequest,
} from "@/types/auth"
import type { ApiResponse } from "@/types/api"

export const authApi = {
  register: (body: RegisterRequest) => unwrap(api.post<ApiResponse<AuthResponse>>("/auth/register", body)),

  login: (body: LoginRequest) => unwrap(api.post<ApiResponse<AuthResponse>>("/auth/login", body)),

  refresh: (refreshToken: string) =>
    unwrap(api.post<ApiResponse<AuthResponse>>("/auth/refresh", { refreshToken })),

  logout: (refreshToken: string) => api.post("/auth/logout", { refreshToken }),

  forgotPassword: (body: ForgotPasswordRequest) => api.post("/auth/forgot-password", body),

  resetPassword: (body: ResetPasswordRequest) => api.post("/auth/reset-password", body),

  verifyEmail: (body: VerifyEmailRequest) => api.post("/auth/verify-email", body),
}
