import { api, unwrap } from "./axios"
import type { ApiResponse, PageResponse } from "@/types/api"
import type { Transaction, TransactionFilters, TransactionRequest } from "@/types/transaction"

export const transactionApi = {
  list: (filters: TransactionFilters = {}) =>
    unwrap(api.get<ApiResponse<PageResponse<Transaction>>>("/transactions", { params: filters })),

  get: (id: string) => unwrap(api.get<ApiResponse<Transaction>>(`/transactions/${id}`)),

  create: (body: TransactionRequest) =>
    unwrap(api.post<ApiResponse<Transaction>>("/transactions", body)),

  update: (id: string, body: TransactionRequest) =>
    unwrap(api.put<ApiResponse<Transaction>>(`/transactions/${id}`, body)),

  remove: (id: string) => api.delete(`/transactions/${id}`),
}
