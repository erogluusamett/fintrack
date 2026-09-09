import { useMutation } from "@tanstack/react-query"
import { userApi } from "@/api/user.api"
import { useAuthStore } from "@/store/auth-store"
import type { ChangePasswordRequest, UpdateProfileRequest } from "@/types/user"

export function useUpdateProfile() {
  return useMutation({
    mutationFn: (body: UpdateProfileRequest) => userApi.updateProfile(body),
    onSuccess: (user) => useAuthStore.getState().setUser(user),
  })
}

export function useChangePassword() {
  return useMutation({
    mutationFn: (body: ChangePasswordRequest) => userApi.changePassword(body),
  })
}
