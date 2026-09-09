import { api, unwrap } from "./axios"
import type { ApiResponse } from "@/types/api"
import type { RecurringTransaction, RecurringTransactionRequest } from "@/types/recurring"

export const recurringApi = {
  list: () => unwrap(api.get<ApiResponse<RecurringTransaction[]>>("/recurring-transactions")),

  create: (body: RecurringTransactionRequest) =>
    unwrap(api.post<ApiResponse<RecurringTransaction>>("/recurring-transactions", body)),

  update: (id: string, body: RecurringTransactionRequest) =>
    unwrap(api.put<ApiResponse<RecurringTransaction>>(`/recurring-transactions/${id}`, body)),

  remove: (id: string) => api.delete(`/recurring-transactions/${id}`),

  pause: (id: string) => unwrap(api.post<ApiResponse<RecurringTransaction>>(`/recurring-transactions/${id}/pause`)),

  resume: (id: string) => unwrap(api.post<ApiResponse<RecurringTransaction>>(`/recurring-transactions/${id}/resume`)),
}
