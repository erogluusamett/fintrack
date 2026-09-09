import { api, unwrap } from "./axios"
import type { ApiResponse } from "@/types/api"
import type { Budget, BudgetRequest, BudgetStatusResponse } from "@/types/budget"

export const budgetApi = {
  list: () => unwrap(api.get<ApiResponse<Budget[]>>("/budgets")),

  create: (body: BudgetRequest) => unwrap(api.post<ApiResponse<Budget>>("/budgets", body)),

  update: (id: string, body: BudgetRequest) => unwrap(api.put<ApiResponse<Budget>>(`/budgets/${id}`, body)),

  remove: (id: string) => api.delete(`/budgets/${id}`),

  status: (id: string) => unwrap(api.get<ApiResponse<BudgetStatusResponse>>(`/budgets/${id}/status`)),
}
