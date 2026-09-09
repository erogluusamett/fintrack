import type { Currency } from "./enums"

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  defaultCurrency: Currency
  emailVerified: boolean
  roles: string[]
  createdAt: string
}

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
  defaultCurrency: Currency
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}
