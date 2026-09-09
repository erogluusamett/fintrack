import { AxiosError } from "axios"
import type { ApiErrorResponse } from "@/types/api"

/**
 * Backend hata mesajları Türkçe (bkz. fintrack backend'in kendi
 * GlobalExceptionHandler'ı) — bu İngilizce arayüzde doğrudan gösterilmez.
 * HTTP status'a göre kendi İngilizce mesajımızı üretiyoruz; backend'in
 * `message` alanı yalnızca beklenmedik (status eşleşmeyen) durumlarda
 * son çare olarak kullanılır.
 */
const STATUS_MESSAGES: Record<number, string> = {
  400: "Please check your input and try again.",
  401: "Your session has expired. Please sign in again.",
  403: "You don't have permission to do that.",
  404: "The requested resource could not be found.",
  409: "This conflicts with something that already exists.",
  422: "That action isn't allowed right now.",
  429: "Too many attempts — please wait a moment and try again.",
  500: "Something went wrong on our end. Please try again shortly.",
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    if (!error.response) {
      return "Unable to connect to the server. Please check your internet connection."
    }

    const status = error.response.status
    if (STATUS_MESSAGES[status]) {
      return STATUS_MESSAGES[status]
    }

    const data = error.response.data as ApiErrorResponse | undefined
    if (data?.message) {
      return data.message
    }
  }

  if (error instanceof Error) {
    return error.message
  }

  return "An unexpected error occurred."
}

export function getFieldErrors(error: unknown): Record<string, string> {
  if (error instanceof AxiosError && error.response?.status === 400) {
    const data = error.response.data as ApiErrorResponse | undefined
    if (data?.fieldErrors) {
      return Object.fromEntries(data.fieldErrors.map((fe) => [fe.field, fe.message]))
    }
  }
  return {}
}
