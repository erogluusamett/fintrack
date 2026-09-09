import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query"
import { budgetApi } from "@/api/budget.api"
import { queryKeys } from "@/api/query-keys"
import type { BudgetRequest } from "@/types/budget"

export function useBudgets() {
  return useQuery({
    queryKey: queryKeys.budgets,
    queryFn: () => budgetApi.list(),
  })
}

/**
 * Backend, bütçe tanımı ile kullanım durumunu ayrı endpoint'lerde tutuyor
 * (GET /budgets vs GET /budgets/{id}/status) — bu yüzden liste + her bütçe
 * için paralel bir status sorgusu gerekiyor. useQueries tam bu senaryo için:
 * N bağımsız query'yi tek bir hook çağrısıyla yönetir.
 */
export function useBudgetStatuses(budgetIds: string[]) {
  return useQueries({
    queries: budgetIds.map((id) => ({
      queryKey: queryKeys.budgetStatus(id),
      queryFn: () => budgetApi.status(id),
    })),
  })
}

function useInvalidateBudgetQueries() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.budgets })
    queryClient.invalidateQueries({ queryKey: ["analytics"] })
  }
}

export function useCreateBudget() {
  const invalidate = useInvalidateBudgetQueries()
  return useMutation({
    mutationFn: (body: BudgetRequest) => budgetApi.create(body),
    onSuccess: invalidate,
  })
}

export function useUpdateBudget() {
  const invalidate = useInvalidateBudgetQueries()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: BudgetRequest }) => budgetApi.update(id, body),
    onSuccess: invalidate,
  })
}

export function useDeleteBudget() {
  const invalidate = useInvalidateBudgetQueries()
  return useMutation({
    mutationFn: (id: string) => budgetApi.remove(id),
    onSuccess: invalidate,
  })
}
