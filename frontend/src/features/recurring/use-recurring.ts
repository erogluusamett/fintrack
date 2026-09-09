import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { recurringApi } from "@/api/recurring.api"
import { queryKeys } from "@/api/query-keys"
import type { RecurringTransactionRequest } from "@/types/recurring"

export function useRecurringTransactions() {
  return useQuery({
    queryKey: queryKeys.recurring,
    queryFn: () => recurringApi.list(),
  })
}

function useInvalidateRecurringQueries() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.recurring })
    queryClient.invalidateQueries({ queryKey: ["analytics"] })
  }
}

export function useCreateRecurringTransaction() {
  const invalidate = useInvalidateRecurringQueries()
  return useMutation({
    mutationFn: (body: RecurringTransactionRequest) => recurringApi.create(body),
    onSuccess: invalidate,
  })
}

export function useUpdateRecurringTransaction() {
  const invalidate = useInvalidateRecurringQueries()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: RecurringTransactionRequest }) => recurringApi.update(id, body),
    onSuccess: invalidate,
  })
}

export function useDeleteRecurringTransaction() {
  const invalidate = useInvalidateRecurringQueries()
  return useMutation({
    mutationFn: (id: string) => recurringApi.remove(id),
    onSuccess: invalidate,
  })
}

export function usePauseRecurringTransaction() {
  const invalidate = useInvalidateRecurringQueries()
  return useMutation({
    mutationFn: (id: string) => recurringApi.pause(id),
    onSuccess: invalidate,
  })
}

export function useResumeRecurringTransaction() {
  const invalidate = useInvalidateRecurringQueries()
  return useMutation({
    mutationFn: (id: string) => recurringApi.resume(id),
    onSuccess: invalidate,
  })
}
