import { api, unwrap } from "./axios"
import type { ApiResponse } from "@/types/api"
import type { ChangePasswordRequest, UpdateProfileRequest, User } from "@/types/user"

export const userApi = {
  getMe: () => unwrap(api.get<ApiResponse<User>>("/users/me")),

  updateProfile: (body: UpdateProfileRequest) => unwrap(api.put<ApiResponse<User>>("/users/me", body)),

  changePassword: (body: ChangePasswordRequest) => api.put("/users/me/password", body),
}
